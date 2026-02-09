# Multi-Agent System with Python MCP Integration and JIRA Flow Mapping

## Overview

This guide shows how to integrate **Python-based MCP servers** that return data to file paths, and use a **CSV mapping file** to fetch JIRA content for specific flows.

---

## Architecture with Python MCP Servers

```
┌─────────────────────────────────────────────────────────────┐
│                    Configuration Layer                       │
│  - flows-config.yaml (all flows)                            │
│  - jira-flow-mapping.csv (flow → JIRA number mapping)       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Python MCP Servers (main.py)                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  JIRA MCP    │  │   DB MCP     │  │  HIVE MCP    │      │
│  │ (Python)     │  │  (Python)    │  │  (Python)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                  │               │
│         ▼                 ▼                  ▼               │
│  Write to paths:                                            │
│  - /mnt/mcp-outputs/jira/{flow_name}/                       │
│  - /mnt/mcp-outputs/db/{flow_name}/                         │
│  - /mnt/mcp-outputs/hive/{flow_name}/                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Claude Code Agents                       │
│  - Read MCP outputs from file paths                         │
│  - Process flow-specific data                               │
│  - Generate requirements                                     │
└─────────────────────────────────────────────────────────────┘
```

---

## File Structure

```
requirement-gathering-system/
│
├── src/main/resources/
│   └── jira-flow-mapping.csv        # Flow → JIRA number mapping
│
├── config/
│   ├── flows-config.yaml
│   ├── agent-config.yaml
│   └── prompts/
│
├── mcp-servers/
│   ├── main.py                      # Main MCP orchestrator
│   ├── jira_mcp.py                  # JIRA MCP server
│   ├── db_mcp.py                    # Database MCP server
│   ├── hive_mcp.py                  # Hive MCP server
│   └── requirements.txt
│
├── mcp-outputs/                     # MCP servers write here
│   ├── jira/
│   │   ├── user_authentication_flow/
│   │   │   ├── jira_content.json
│   │   │   └── metadata.json
│   │   └── payment_processing_flow/
│   ├── db/
│   │   ├── user_authentication_flow/
│   │   │   └── schema.json
│   │   └── payment_processing_flow/
│   └── hive/
│       ├── user_authentication_flow/
│       └── payment_processing_flow/
│
├── agents/
│   ├── base_agent.py
│   ├── flow_orchestrator.py
│   ├── code_analysis_agent.py
│   └── ...
│
├── shared/
│   ├── mcp_client.py                # Reads from MCP output paths
│   ├── jira_flow_mapper.py          # NEW: CSV mapper
│   └── ...
│
├── outputs/                         # Final outputs
│   └── {flow_name}/
│
├── run_mcp_servers.py               # NEW: Start MCP servers
└── run.py                           # Main orchestrator
```

---

## 1. JIRA-Flow Mapping CSV

### src/main/resources/jira-flow-mapping.csv

```csv
flow_name,jira_number,jira_type,priority,team
user_authentication_flow,PROJ-1234,Epic,High,Auth Team
user_authentication_flow,PROJ-1235,Story,High,Auth Team
user_authentication_flow,PROJ-1236,Story,Medium,Auth Team
payment_processing_flow,PROJ-2001,Epic,Critical,Payments Team
payment_processing_flow,PROJ-2002,Story,High,Payments Team
order_fulfillment_flow,PROJ-3001,Epic,Medium,Fulfillment Team
notification_flow,PROJ-4001,Story,Low,Notifications Team
reporting_flow,PROJ-5001,Epic,Low,Analytics Team
```

**Columns:**
- `flow_name`: Must match flow name in flows-config.yaml
- `jira_number`: JIRA ticket ID (e.g., PROJ-1234)
- `jira_type`: Epic, Story, Task, Bug, etc.
- `priority`: Critical, High, Medium, Low
- `team`: Owning team (optional)

---

## 2. JIRA-Flow Mapper

### shared/jira_flow_mapper.py

```python
# shared/jira_flow_mapper.py

import csv
from typing import List, Dict, Optional
from pathlib import Path
import structlog

logger = structlog.get_logger()

class JiraFlowMapper:
    """Maps flows to their associated JIRA tickets from CSV"""
    
    def __init__(self, csv_path: str = 'src/main/resources/jira-flow-mapping.csv'):
        self.csv_path = Path(csv_path)
        self.mapping: Dict[str, List[Dict]] = {}
        self._load_mapping()
        
    def _load_mapping(self):
        """Load JIRA-flow mapping from CSV"""
        if not self.csv_path.exists():
            logger.warning(
                "JIRA mapping CSV not found",
                path=str(self.csv_path)
            )
            return
            
        with open(self.csv_path, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                flow_name = row['flow_name'].strip()
                
                if flow_name not in self.mapping:
                    self.mapping[flow_name] = []
                    
                self.mapping[flow_name].append({
                    'jira_number': row['jira_number'].strip(),
                    'jira_type': row.get('jira_type', '').strip(),
                    'priority': row.get('priority', '').strip(),
                    'team': row.get('team', '').strip()
                })
                
        logger.info(
            "Loaded JIRA-flow mapping",
            total_flows=len(self.mapping),
            total_tickets=sum(len(v) for v in self.mapping.values())
        )
        
    def get_jira_tickets(self, flow_name: str) -> List[Dict]:
        """
        Get all JIRA tickets associated with a flow
        
        Args:
            flow_name: Name of the flow
            
        Returns:
            List of JIRA ticket dictionaries
        """
        return self.mapping.get(flow_name, [])
        
    def get_jira_numbers(self, flow_name: str) -> List[str]:
        """
        Get just the JIRA ticket numbers for a flow
        
        Args:
            flow_name: Name of the flow
            
        Returns:
            List of JIRA ticket IDs
        """
        tickets = self.get_jira_tickets(flow_name)
        return [t['jira_number'] for t in tickets]
        
    def has_jira_tickets(self, flow_name: str) -> bool:
        """Check if flow has associated JIRA tickets"""
        return flow_name in self.mapping and len(self.mapping[flow_name]) > 0
        
    def get_all_flows(self) -> List[str]:
        """Get all flow names that have JIRA tickets"""
        return list(self.mapping.keys())
        
    def get_stats(self, flow_name: str) -> Dict:
        """Get statistics about JIRA tickets for a flow"""
        tickets = self.get_jira_tickets(flow_name)
        
        if not tickets:
            return {
                'total_tickets': 0,
                'by_type': {},
                'by_priority': {}
            }
            
        by_type = {}
        by_priority = {}
        
        for ticket in tickets:
            jira_type = ticket.get('jira_type', 'Unknown')
            priority = ticket.get('priority', 'Unknown')
            
            by_type[jira_type] = by_type.get(jira_type, 0) + 1
            by_priority[priority] = by_priority.get(priority, 0) + 1
            
        return {
            'total_tickets': len(tickets),
            'by_type': by_type,
            'by_priority': by_priority,
            'teams': list(set(t.get('team', '') for t in tickets if t.get('team')))
        }

# Example usage
mapper = JiraFlowMapper()
tickets = mapper.get_jira_tickets('user_authentication_flow')
print(f"Found {len(tickets)} JIRA tickets for user_authentication_flow")
```

---

## 3. Python MCP Servers

### mcp-servers/main.py

```python
# mcp-servers/main.py

"""
Main orchestrator for Python-based MCP servers
Runs JIRA, DB, and Hive MCP servers and writes outputs to file paths
"""

import argparse
import json
from pathlib import Path
from typing import Dict, List
import structlog
from jira_mcp import JiraMCP
from db_mcp import DatabaseMCP
from hive_mcp import HiveMCP

logger = structlog.get_logger()

class MCPOrchestrator:
    """Orchestrates all MCP servers for a flow"""
    
    def __init__(self, output_base_dir: str = 'mcp-outputs'):
        self.output_base_dir = Path(output_base_dir)
        self.jira_mcp = JiraMCP()
        self.db_mcp = DatabaseMCP()
        self.hive_mcp = HiveMCP()
        
    def run_for_flow(self, flow_name: str, jira_numbers: List[str] = None):
        """
        Run all MCP servers for a specific flow
        
        Args:
            flow_name: Name of the flow
            jira_numbers: Optional list of JIRA ticket numbers
        """
        logger.info("Running MCP servers", flow_name=flow_name)
        
        # Create output directories
        flow_output_dir = self.output_base_dir / flow_name
        flow_output_dir.mkdir(parents=True, exist_ok=True)
        
        results = {}
        
        # Run JIRA MCP (only if JIRA numbers provided)
        if jira_numbers:
            logger.info("Running JIRA MCP", flow_name=flow_name, tickets=len(jira_numbers))
            jira_output = self.jira_mcp.fetch_tickets(jira_numbers, flow_name)
            jira_path = self.output_base_dir / 'jira' / flow_name
            jira_path.mkdir(parents=True, exist_ok=True)
            
            # Write JIRA content
            with open(jira_path / 'jira_content.json', 'w') as f:
                json.dump(jira_output, f, indent=2)
                
            results['jira'] = str(jira_path / 'jira_content.json')
            logger.info("JIRA MCP completed", output_path=results['jira'])
        else:
            logger.info("Skipping JIRA MCP (no tickets)", flow_name=flow_name)
            
        # Run Database MCP
        logger.info("Running Database MCP", flow_name=flow_name)
        db_output = self.db_mcp.extract_schema(flow_name)
        db_path = self.output_base_dir / 'db' / flow_name
        db_path.mkdir(parents=True, exist_ok=True)
        
        with open(db_path / 'schema.json', 'w') as f:
            json.dump(db_output, f, indent=2)
            
        results['db'] = str(db_path / 'schema.json')
        logger.info("Database MCP completed", output_path=results['db'])
        
        # Run Hive MCP
        logger.info("Running Hive MCP", flow_name=flow_name)
        hive_output = self.hive_mcp.extract_schema(flow_name)
        hive_path = self.output_base_dir / 'hive' / flow_name
        hive_path.mkdir(parents=True, exist_ok=True)
        
        with open(hive_path / 'schema.json', 'w') as f:
            json.dump(hive_output, f, indent=2)
            
        results['hive'] = str(hive_path / 'schema.json')
        logger.info("Hive MCP completed", output_path=results['hive'])
        
        # Write metadata
        metadata = {
            'flow_name': flow_name,
            'jira_tickets': jira_numbers or [],
            'outputs': results,
            'timestamp': self._get_timestamp()
        }
        
        with open(flow_output_dir / 'metadata.json', 'w') as f:
            json.dump(metadata, f, indent=2)
            
        logger.info(
            "MCP orchestration completed",
            flow_name=flow_name,
            outputs=results
        )
        
        return results
        
    def _get_timestamp(self):
        from datetime import datetime
        return datetime.now().isoformat()

def main():
    parser = argparse.ArgumentParser(description='Run MCP servers for a flow')
    parser.add_argument('--flow-name', required=True, help='Flow name')
    parser.add_argument('--jira-tickets', nargs='*', help='JIRA ticket numbers')
    parser.add_argument('--output-dir', default='mcp-outputs', help='Output directory')
    
    args = parser.parse_args()
    
    orchestrator = MCPOrchestrator(args.output_dir)
    results = orchestrator.run_for_flow(args.flow_name, args.jira_tickets)
    
    print(f"\n✅ MCP servers completed for {args.flow_name}")
    print(f"Outputs:")
    for mcp_type, path in results.items():
        print(f"  - {mcp_type}: {path}")

if __name__ == "__main__":
    main()
```

---

### mcp-servers/jira_mcp.py

```python
# mcp-servers/jira_mcp.py

"""
JIRA MCP Server - Fetches JIRA ticket content
"""

import json
from typing import List, Dict
from jira import JIRA
import os
import structlog

logger = structlog.get_logger()

class JiraMCP:
    """JIRA MCP Server - returns data to file paths"""
    
    def __init__(self):
        # Initialize JIRA client
        self.jira_url = os.getenv('JIRA_URL', 'https://your-company.atlassian.net')
        self.jira_token = os.getenv('JIRA_API_TOKEN')
        self.jira_email = os.getenv('JIRA_EMAIL')
        
        if self.jira_token and self.jira_email:
            self.client = JIRA(
                server=self.jira_url,
                basic_auth=(self.jira_email, self.jira_token)
            )
        else:
            logger.warning("JIRA credentials not found, using mock data")
            self.client = None
            
    def fetch_tickets(self, ticket_numbers: List[str], flow_name: str) -> Dict:
        """
        Fetch JIRA ticket content for given ticket numbers
        
        Args:
            ticket_numbers: List of JIRA ticket IDs (e.g., ['PROJ-1234', 'PROJ-1235'])
            flow_name: Name of the flow (for context)
            
        Returns:
            Dictionary with JIRA ticket data
        """
        logger.info(
            "Fetching JIRA tickets",
            flow_name=flow_name,
            ticket_count=len(ticket_numbers)
        )
        
        tickets_data = []
        
        for ticket_number in ticket_numbers:
            try:
                if self.client:
                    # Real JIRA fetch
                    issue = self.client.issue(ticket_number)
                    ticket_data = self._parse_issue(issue)
                else:
                    # Mock data for testing
                    ticket_data = self._mock_ticket_data(ticket_number)
                    
                tickets_data.append(ticket_data)
                logger.info("Fetched ticket", ticket=ticket_number)
                
            except Exception as e:
                logger.error(
                    "Failed to fetch ticket",
                    ticket=ticket_number,
                    error=str(e)
                )
                # Add error placeholder
                tickets_data.append({
                    'key': ticket_number,
                    'error': str(e)
                })
                
        return {
            'flow_name': flow_name,
            'ticket_count': len(tickets_data),
            'tickets': tickets_data,
            'metadata': {
                'jira_url': self.jira_url,
                'requested_tickets': ticket_numbers
            }
        }
        
    def _parse_issue(self, issue) -> Dict:
        """Parse JIRA issue object into dictionary"""
        return {
            'key': issue.key,
            'summary': issue.fields.summary,
            'description': issue.fields.description or '',
            'type': issue.fields.issuetype.name,
            'status': issue.fields.status.name,
            'priority': issue.fields.priority.name if issue.fields.priority else 'None',
            'assignee': issue.fields.assignee.displayName if issue.fields.assignee else 'Unassigned',
            'reporter': issue.fields.reporter.displayName if issue.fields.reporter else 'Unknown',
            'created': str(issue.fields.created),
            'updated': str(issue.fields.updated),
            'labels': issue.fields.labels or [],
            'components': [c.name for c in issue.fields.components] if issue.fields.components else [],
            'acceptance_criteria': self._extract_acceptance_criteria(issue),
            'comments': self._extract_comments(issue),
            'attachments': self._extract_attachments(issue)
        }
        
    def _extract_acceptance_criteria(self, issue) -> str:
        """Extract acceptance criteria from JIRA ticket"""
        # Check custom field (adjust field ID as needed)
        try:
            if hasattr(issue.fields, 'customfield_10100'):
                return issue.fields.customfield_10100 or ''
        except:
            pass
            
        # Fallback: parse from description
        description = issue.fields.description or ''
        if 'Acceptance Criteria' in description:
            parts = description.split('Acceptance Criteria')
            if len(parts) > 1:
                return parts[1].split('\n\n')[0].strip()
                
        return ''
        
    def _extract_comments(self, issue) -> List[Dict]:
        """Extract comments from JIRA ticket"""
        comments = []
        for comment in issue.fields.comment.comments[:5]:  # Last 5 comments
            comments.append({
                'author': comment.author.displayName,
                'body': comment.body,
                'created': str(comment.created)
            })
        return comments
        
    def _extract_attachments(self, issue) -> List[Dict]:
        """Extract attachment information"""
        attachments = []
        for attachment in issue.fields.attachment:
            attachments.append({
                'filename': attachment.filename,
                'size': attachment.size,
                'url': attachment.content
            })
        return attachments
        
    def _mock_ticket_data(self, ticket_number: str) -> Dict:
        """Generate mock ticket data for testing"""
        return {
            'key': ticket_number,
            'summary': f'Mock summary for {ticket_number}',
            'description': f'Mock description for {ticket_number}',
            'type': 'Story',
            'status': 'In Progress',
            'priority': 'High',
            'assignee': 'Mock User',
            'reporter': 'Mock Reporter',
            'created': '2024-01-01T00:00:00',
            'updated': '2024-01-15T00:00:00',
            'labels': ['backend', 'api'],
            'components': ['Authentication'],
            'acceptance_criteria': '- User can login\n- Session persists',
            'comments': [
                {
                    'author': 'Dev User',
                    'body': 'Working on implementation',
                    'created': '2024-01-10T00:00:00'
                }
            ],
            'attachments': []
        }
```

---

### mcp-servers/db_mcp.py

```python
# mcp-servers/db_mcp.py

"""
Database MCP Server - Extracts database schemas
"""

import json
from typing import Dict, List
import psycopg2
import os
import structlog

logger = structlog.get_logger()

class DatabaseMCP:
    """Database MCP Server - returns schema data to file paths"""
    
    def __init__(self):
        # Initialize database connection
        self.db_host = os.getenv('DB_HOST', 'localhost')
        self.db_port = os.getenv('DB_PORT', '5432')
        self.db_name = os.getenv('DB_NAME', 'mydb')
        self.db_user = os.getenv('DB_USER', 'user')
        self.db_password = os.getenv('DB_PASSWORD', 'password')
        
        try:
            self.connection = psycopg2.connect(
                host=self.db_host,
                port=self.db_port,
                database=self.db_name,
                user=self.db_user,
                password=self.db_password
            )
            logger.info("Database connection established")
        except Exception as e:
            logger.warning(f"Database connection failed: {e}, using mock data")
            self.connection = None
            
    def extract_schema(self, flow_name: str) -> Dict:
        """
        Extract database schema relevant to the flow
        
        Args:
            flow_name: Name of the flow
            
        Returns:
            Dictionary with schema information
        """
        logger.info("Extracting database schema", flow_name=flow_name)
        
        # Determine which tables are relevant to this flow
        # Convention: tables prefixed with flow_name or referenced in flow config
        table_prefix = flow_name.replace('_flow', '')
        
        if self.connection:
            tables = self._get_tables_by_prefix(table_prefix)
        else:
            tables = self._mock_tables(table_prefix)
            
        schema_data = {
            'flow_name': flow_name,
            'database': self.db_name,
            'table_count': len(tables),
            'tables': tables,
            'relationships': self._extract_relationships(tables) if self.connection else []
        }
        
        return schema_data
        
    def _get_tables_by_prefix(self, prefix: str) -> List[Dict]:
        """Get all tables matching prefix"""
        cursor = self.connection.cursor()
        
        # Get tables
        cursor.execute("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name LIKE %s
        """, (f'{prefix}%',))
        
        table_names = [row[0] for row in cursor.fetchall()]
        
        tables = []
        for table_name in table_names:
            table_info = self._describe_table(table_name)
            tables.append(table_info)
            
        cursor.close()
        return tables
        
    def _describe_table(self, table_name: str) -> Dict:
        """Get detailed schema for a table"""
        cursor = self.connection.cursor()
        
        cursor.execute("""
            SELECT 
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns
            WHERE table_name = %s
            ORDER BY ordinal_position
        """, (table_name,))
        
        columns = []
        for row in cursor.fetchall():
            columns.append({
                'name': row[0],
                'type': row[1],
                'max_length': row[2],
                'nullable': row[3] == 'YES',
                'default': row[4]
            })
            
        # Get indexes
        cursor.execute("""
            SELECT indexname, indexdef
            FROM pg_indexes
            WHERE tablename = %s
        """, (table_name,))
        
        indexes = [
            {'name': row[0], 'definition': row[1]}
            for row in cursor.fetchall()
        ]
        
        cursor.close()
        
        return {
            'table_name': table_name,
            'columns': columns,
            'indexes': indexes
        }
        
    def _extract_relationships(self, tables: List[Dict]) -> List[Dict]:
        """Extract foreign key relationships"""
        cursor = self.connection.cursor()
        
        relationships = []
        for table in tables:
            table_name = table['table_name']
            
            cursor.execute("""
                SELECT
                    tc.constraint_name,
                    kcu.column_name,
                    ccu.table_name AS foreign_table_name,
                    ccu.column_name AS foreign_column_name
                FROM information_schema.table_constraints AS tc
                JOIN information_schema.key_column_usage AS kcu
                    ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage AS ccu
                    ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY'
                AND tc.table_name = %s
            """, (table_name,))
            
            for row in cursor.fetchall():
                relationships.append({
                    'from_table': table_name,
                    'from_column': row[1],
                    'to_table': row[2],
                    'to_column': row[3],
                    'constraint_name': row[0]
                })
                
        cursor.close()
        return relationships
        
    def _mock_tables(self, prefix: str) -> List[Dict]:
        """Generate mock table data"""
        return [
            {
                'table_name': f'{prefix}_users',
                'columns': [
                    {'name': 'id', 'type': 'uuid', 'nullable': False, 'default': 'gen_random_uuid()'},
                    {'name': 'email', 'type': 'varchar', 'max_length': 255, 'nullable': False},
                    {'name': 'created_at', 'type': 'timestamp', 'nullable': False, 'default': 'now()'}
                ],
                'indexes': [
                    {'name': f'{prefix}_users_pkey', 'definition': 'PRIMARY KEY (id)'}
                ]
            }
        ]
        
    def __del__(self):
        if self.connection:
            self.connection.close()
```

---

### mcp-servers/hive_mcp.py

```python
# mcp-servers/hive_mcp.py

"""
Hive MCP Server - Extracts Hive table schemas
"""

from pyhive import hive
import os
import structlog

logger = structlog.get_logger()

class HiveMCP:
    """Hive MCP Server - returns schema data to file paths"""
    
    def __init__(self):
        self.hive_host = os.getenv('HIVE_HOST', 'localhost')
        self.hive_port = int(os.getenv('HIVE_PORT', '10000'))
        
        try:
            self.connection = hive.Connection(
                host=self.hive_host,
                port=self.hive_port
            )
            logger.info("Hive connection established")
        except Exception as e:
            logger.warning(f"Hive connection failed: {e}, using mock data")
            self.connection = None
            
    def extract_schema(self, flow_name: str) -> Dict:
        """
        Extract Hive table schema relevant to the flow
        
        Args:
            flow_name: Name of the flow
            
        Returns:
            Dictionary with Hive schema information
        """
        logger.info("Extracting Hive schema", flow_name=flow_name)
        
        table_prefix = flow_name.replace('_flow', '')
        
        if self.connection:
            tables = self._get_hive_tables(table_prefix)
        else:
            tables = self._mock_hive_tables(table_prefix)
            
        return {
            'flow_name': flow_name,
            'warehouse': 'hive',
            'table_count': len(tables),
            'tables': tables
        }
        
    def _get_hive_tables(self, prefix: str) -> List[Dict]:
        """Get Hive tables by prefix"""
        cursor = self.connection.cursor()
        
        # Show tables
        cursor.execute(f"SHOW TABLES LIKE '{prefix}*'")
        table_names = [row[0] for row in cursor.fetchall()]
        
        tables = []
        for table_name in table_names:
            cursor.execute(f"DESCRIBE {table_name}")
            columns = [
                {
                    'name': row[0],
                    'type': row[1],
                    'comment': row[2] if len(row) > 2 else ''
                }
                for row in cursor.fetchall()
            ]
            
            # Get partition info
            cursor.execute(f"SHOW PARTITIONS {table_name}")
            partitions = [row[0] for row in cursor.fetchall()]
            
            tables.append({
                'table_name': table_name,
                'columns': columns,
                'partitions': partitions
            })
            
        cursor.close()
        return tables
        
    def _mock_hive_tables(self, prefix: str) -> List[Dict]:
        """Generate mock Hive table data"""
        return [
            {
                'table_name': f'{prefix}_events',
                'columns': [
                    {'name': 'event_id', 'type': 'string', 'comment': 'Event identifier'},
                    {'name': 'timestamp', 'type': 'timestamp', 'comment': 'Event time'},
                    {'name': 'user_id', 'type': 'string', 'comment': 'User identifier'},
                    {'name': 'event_type', 'type': 'string', 'comment': 'Type of event'}
                ],
                'partitions': ['dt=2024-01-01', 'dt=2024-01-02']
            }
        ]
```

---

## 4. MCP Client (Reads from File Paths)

### shared/mcp_client.py

```python
# shared/mcp_client.py

"""
MCP Client - Reads data from MCP output file paths
"""

import json
from pathlib import Path
from typing import Dict, Optional
import structlog

logger = structlog.get_logger()

class MCPClient:
    """Client to read MCP outputs from file system"""
    
    def __init__(self, mcp_output_base: str = 'mcp-outputs'):
        self.base_path = Path(mcp_output_base)
        
    def get_jira_data(self, flow_name: str) -> Optional[Dict]:
        """
        Get JIRA data for a flow
        
        Args:
            flow_name: Name of the flow
            
        Returns:
            JIRA data dictionary or None if not found
        """
        jira_path = self.base_path / 'jira' / flow_name / 'jira_content.json'
        
        if not jira_path.exists():
            logger.warning("JIRA data not found", flow_name=flow_name, path=str(jira_path))
            return None
            
        with open(jira_path, 'r') as f:
            data = json.load(f)
            
        logger.info("Loaded JIRA data", flow_name=flow_name, tickets=data.get('ticket_count', 0))
        return data
        
    def get_db_schema(self, flow_name: str) -> Optional[Dict]:
        """Get database schema for a flow"""
        db_path = self.base_path / 'db' / flow_name / 'schema.json'
        
        if not db_path.exists():
            logger.warning("DB schema not found", flow_name=flow_name)
            return None
            
        with open(db_path, 'r') as f:
            data = json.load(f)
            
        logger.info("Loaded DB schema", flow_name=flow_name, tables=data.get('table_count', 0))
        return data
        
    def get_hive_schema(self, flow_name: str) -> Optional[Dict]:
        """Get Hive schema for a flow"""
        hive_path = self.base_path / 'hive' / flow_name / 'schema.json'
        
        if not hive_path.exists():
            logger.warning("Hive schema not found", flow_name=flow_name)
            return None
            
        with open(hive_path, 'r') as f:
            data = json.load(f)
            
        logger.info("Loaded Hive schema", flow_name=flow_name, tables=data.get('table_count', 0))
        return data
        
    def get_all_mcp_data(self, flow_name: str) -> Dict:
        """Get all MCP data for a flow"""
        return {
            'jira': self.get_jira_data(flow_name),
            'db': self.get_db_schema(flow_name),
            'hive': self.get_hive_schema(flow_name)
        }
```

---

## 5. Enhanced Flow Orchestrator with MCP Integration

### agents/flow_orchestrator.py (Updated)

```python
# agents/flow_orchestrator.py

from typing import Dict, Any
from shared.flow_context import FlowContext
from shared.mcp_client import MCPClient
from shared.jira_flow_mapper import JiraFlowMapper
import subprocess
import structlog

logger = structlog.get_logger()

class FlowOrchestrator:
    """Orchestrates all agents for a single flow with MCP integration"""
    
    def __init__(self, config: Dict, flow_context: FlowContext):
        self.config = config
        self.flow_context = flow_context
        self.flow_name = flow_context.flow_name
        self.mcp_client = MCPClient()
        self.jira_mapper = JiraFlowMapper()
        
        # Initialize agents...
        
    def run_full_pipeline(self, codebase_path: str) -> Dict[str, Any]:
        """Execute complete pipeline with MCP data"""
        
        logger.info("Starting flow pipeline with MCP integration", flow_name=self.flow_name)
        
        results = {'flow_name': self.flow_name}
        
        # STEP 0: Run MCP servers to gather external data
        logger.info("Step 0: Running MCP servers", flow_name=self.flow_name)
        mcp_data = self._run_mcp_servers()
        results['mcp_data'] = mcp_data
        
        # STEP 1: Code Analysis (with MCP data context)
        logger.info("Step 1: Code Analysis", flow_name=self.flow_name)
        code_analysis = self.agents['code_analysis'].run({
            'codebase_path': codebase_path,
            'mcp_data': mcp_data  # Pass MCP data to agent
        })
        results['code_analysis'] = code_analysis
        
        # STEP 2: Requirement Extraction (enriched with JIRA content)
        logger.info("Step 2: Requirement Extraction", flow_name=self.flow_name)
        requirements = self.agents['requirement_extraction'].run({
            'code_analysis': code_analysis,
            'jira_data': mcp_data.get('jira')  # Include JIRA tickets
        })
        results['requirements'] = requirements
        
        # Continue with other steps...
        
        return results
        
    def _run_mcp_servers(self) -> Dict:
        """
        Run Python MCP servers to gather external data
        
        Returns:
            Dictionary with MCP data from all servers
        """
        # Get JIRA tickets for this flow from CSV mapping
        jira_tickets = self.jira_mapper.get_jira_numbers(self.flow_name)
        
        if jira_tickets:
            logger.info(
                "Found JIRA tickets for flow",
                flow_name=self.flow_name,
                tickets=jira_tickets
            )
        else:
            logger.info("No JIRA tickets found for flow", flow_name=self.flow_name)
        
        # Run MCP servers via subprocess
        cmd = [
            'python',
            'mcp-servers/main.py',
            '--flow-name', self.flow_name,
            '--output-dir', 'mcp-outputs'
        ]
        
        if jira_tickets:
            cmd.extend(['--jira-tickets'] + jira_tickets)
            
        logger.info("Executing MCP servers", command=' '.join(cmd))
        
        result = subprocess.run(cmd, capture_output=True, text=True)
        
        if result.returncode != 0:
            logger.error("MCP servers failed", error=result.stderr)
            # Continue anyway with empty MCP data
            return {}
            
        logger.info("MCP servers completed successfully")
        
        # Read MCP outputs from file paths
        mcp_data = self.mcp_client.get_all_mcp_data(self.flow_name)
        
        return mcp_data
```

---

## 6. Enhanced Code Analysis Agent with MCP Data

### agents/code_analysis_agent.py (Updated)

```python
# agents/code_analysis_agent.py

from agents.base_agent import BaseAgent
from typing import Dict, Any
import json

class CodeAnalysisAgent(BaseAgent):
    """Analyzes codebase with MCP data enrichment"""
    
    def execute(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """
        Analyze code for the current flow with MCP context
        
        Args:
            inputs: {
                'codebase_path': str,
                'mcp_data': Dict  # MCP data from JIRA, DB, Hive
            }
        """
        mcp_data = inputs.get('mcp_data', {})
        
        # Extract relevant MCP context
        db_tables = []
        if mcp_data.get('db'):
            db_tables = [t['table_name'] for t in mcp_data['db'].get('tables', [])]
            
        hive_tables = []
        if mcp_data.get('hive'):
            hive_tables = [t['table_name'] for t in mcp_data['hive'].get('tables', [])]
            
        jira_context = ""
        if mcp_data.get('jira'):
            jira_tickets = mcp_data['jira'].get('tickets', [])
            jira_context = self._format_jira_context(jira_tickets)
        
        # Build enhanced prompt with MCP context
        base_prompt = self.get_prompt_template('code-analysis.md')
        
        enhanced_prompt = f"""
{base_prompt}

## Additional Context from MCP Servers

### Database Tables (from DB MCP)
{json.dumps(db_tables, indent=2)}

### Hive Tables (from Hive MCP)
{json.dumps(hive_tables, indent=2)}

### JIRA Tickets Context
{jira_context}

**Important:** Use this MCP context to:
1. Cross-reference database tables found in code
2. Identify data warehouse dependencies
3. Align code analysis with JIRA ticket descriptions
4. Validate that code implements JIRA requirements
"""
        
        # Run analysis with enhanced prompt
        output_path = self.get_output_path('code-analysis.json')
        
        # Execute Claude Code with enhanced prompt
        # ... (rest of implementation)
        
        return analysis
        
    def _format_jira_context(self, jira_tickets):
        """Format JIRA tickets for prompt context"""
        if not jira_tickets:
            return "No JIRA tickets found for this flow."
            
        context = []
        for ticket in jira_tickets:
            context.append(f"""
**{ticket['key']}**: {ticket['summary']}
- Type: {ticket['type']}
- Status: {ticket['status']}
- Description: {ticket['description'][:200]}...
- Acceptance Criteria: {ticket.get('acceptance_criteria', 'N/A')}
""")
            
        return "\n".join(context)
```

---

## 7. Complete Execution Flow

### run_mcp_servers.py

```python
# run_mcp_servers.py

"""
Pre-run script to execute MCP servers for all flows
"""

from utils.config_loader import FlowConfigLoader
from shared.jira_flow_mapper import JiraFlowMapper
import subprocess
import structlog
from concurrent.futures import ThreadPoolExecutor, as_completed

logger = structlog.get_logger()

def run_mcp_for_flow(flow_name, jira_tickets):
    """Run MCP servers for a single flow"""
    cmd = [
        'python',
        'mcp-servers/main.py',
        '--flow-name', flow_name,
        '--output-dir', 'mcp-outputs'
    ]
    
    if jira_tickets:
        cmd.extend(['--jira-tickets'] + jira_tickets)
        
    logger.info("Running MCP servers", flow_name=flow_name)
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    
    if result.returncode == 0:
        logger.info("MCP servers completed", flow_name=flow_name)
        return {'flow_name': flow_name, 'status': 'success'}
    else:
        logger.error("MCP servers failed", flow_name=flow_name, error=result.stderr)
        return {'flow_name': flow_name, 'status': 'failed', 'error': result.stderr}

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Run MCP servers for all flows')
    parser.add_argument('--parallel', action='store_true', help='Run in parallel')
    parser.add_argument('--flows', nargs='+', help='Specific flows to process')
    
    args = parser.parse_args()
    
    # Load configurations
    flow_loader = FlowConfigLoader()
    jira_mapper = JiraFlowMapper()
    
    # Determine which flows to process
    if args.flows:
        flows = [flow_loader.get_flow_by_name(name) for name in args.flows]
        flows = [f for f in flows if f and f.enabled]
    else:
        flows = flow_loader.get_enabled_flows()
        
    logger.info(f"Running MCP servers for {len(flows)} flows")
    
    results = []
    
    if args.parallel:
        # Parallel execution
        with ThreadPoolExecutor(max_workers=5) as executor:
            futures = []
            for flow in flows:
                jira_tickets = jira_mapper.get_jira_numbers(flow.name)
                future = executor.submit(run_mcp_for_flow, flow.name, jira_tickets)
                futures.append(future)
                
            for future in as_completed(futures):
                results.append(future.result())
    else:
        # Sequential execution
        for flow in flows:
            jira_tickets = jira_mapper.get_jira_numbers(flow.name)
            result = run_mcp_for_flow(flow.name, jira_tickets)
            results.append(result)
            
    # Summary
    successful = sum(1 for r in results if r['status'] == 'success')
    failed = len(results) - successful
    
    print(f"\n✅ MCP servers completed:")
    print(f"   - Successful: {successful}")
    print(f"   - Failed: {failed}")
    
    if failed > 0:
        print(f"\nFailed flows:")
        for r in results:
            if r['status'] == 'failed':
                print(f"   - {r['flow_name']}: {r.get('error', 'Unknown error')}")

if __name__ == "__main__":
    main()
```

---

## 8. Complete Workflow

### Step-by-Step Execution

```bash
# STEP 1: Setup environment variables
export JIRA_URL="https://your-company.atlassian.net"
export JIRA_EMAIL="your-email@company.com"
export JIRA_API_TOKEN="your-jira-token"

export DB_HOST="your-db-host"
export DB_PORT="5432"
export DB_NAME="mydb"
export DB_USER="dbuser"
export DB_PASSWORD="dbpass"

export HIVE_HOST="your-hive-host"
export HIVE_PORT="10000"

# STEP 2: Install MCP server dependencies
cd mcp-servers
pip install -r requirements.txt
cd ..

# STEP 3: Run MCP servers for all flows (pre-processing)
python run_mcp_servers.py --parallel

# This creates:
# mcp-outputs/
#   ├── jira/
#   │   ├── user_authentication_flow/jira_content.json
#   │   ├── payment_processing_flow/jira_content.json
#   ├── db/
#   │   ├── user_authentication_flow/schema.json
#   │   ├── payment_processing_flow/schema.json
#   └── hive/
#       ├── user_authentication_flow/schema.json
#       └── payment_processing_flow/schema.json

# STEP 4: Run requirement gathering (reads from MCP outputs)
python run_all_flows.py --codebase /path/to/repo --parallel

# OR run single flow
python run.py --flow-name user_authentication_flow --codebase /path/to/repo
```

---

## 9. MCP Output Examples

### mcp-outputs/jira/user_authentication_flow/jira_content.json

```json
{
  "flow_name": "user_authentication_flow",
  "ticket_count": 3,
  "tickets": [
    {
      "key": "PROJ-1234",
      "summary": "Implement OAuth2 authentication",
      "description": "Users should be able to login using Google OAuth2...",
      "type": "Epic",
      "status": "In Progress",
      "priority": "High",
      "assignee": "John Doe",
      "acceptance_criteria": "- User can login with Google\n- Session persists for 24 hours\n- Refresh token works",
      "comments": [
        {
          "author": "Jane Smith",
          "body": "Should we support other OAuth providers?",
          "created": "2024-01-10T10:00:00"
        }
      ]
    },
    {
      "key": "PROJ-1235",
      "summary": "Add password reset functionality",
      "description": "Users should be able to reset their password via email...",
      "type": "Story",
      "status": "To Do",
      "priority": "Medium"
    }
  ]
}
```

### mcp-outputs/db/user_authentication_flow/schema.json

```json
{
  "flow_name": "user_authentication_flow",
  "database": "mydb",
  "table_count": 2,
  "tables": [
    {
      "table_name": "user_authentication_users",
      "columns": [
        {"name": "id", "type": "uuid", "nullable": false},
        {"name": "email", "type": "varchar", "max_length": 255, "nullable": false},
        {"name": "password_hash", "type": "varchar", "nullable": true},
        {"name": "oauth_provider", "type": "varchar", "nullable": true},
        {"name": "created_at", "type": "timestamp", "nullable": false}
      ],
      "indexes": [
        {"name": "users_pkey", "definition": "PRIMARY KEY (id)"},
        {"name": "users_email_idx", "definition": "UNIQUE (email)"}
      ]
    },
    {
      "table_name": "user_authentication_sessions",
      "columns": [
        {"name": "id", "type": "uuid", "nullable": false},
        {"name": "user_id", "type": "uuid", "nullable": false},
        {"name": "token", "type": "varchar", "nullable": false},
        {"name": "expires_at", "type": "timestamp", "nullable": false}
      ]
    }
  ],
  "relationships": [
    {
      "from_table": "user_authentication_sessions",
      "from_column": "user_id",
      "to_table": "user_authentication_users",
      "to_column": "id"
    }
  ]
}
```

---

## 10. Key Benefits of This Architecture

✅ **Separation of Concerns**
- MCP servers run independently (Python)
- Claude Code agents read from file paths
- No direct coupling

✅ **Flow-Specific Data**
- Each flow gets its own JIRA tickets (from CSV)
- Each flow gets relevant DB/Hive schemas
- Clean output organization

✅ **Scalability**
- MCP servers can run in parallel
- Agents can run in parallel
- File-based communication = easy to cache

✅ **Traceability**
- All MCP outputs saved to disk
- Easy to debug and audit
- Can rerun agents without re-fetching MCP data

✅ **Flexibility**
- Add new MCP servers easily
- Extend CSV mapping with more columns
- Agents can selectively use MCP data

---

## Summary

This architecture provides:

1. **CSV-based JIRA mapping** (`src/main/resources/jira-flow-mapping.csv`)
2. **Python MCP servers** that write to file paths (`mcp-outputs/`)
3. **MCP client** that reads from those paths
4. **Flow-specific orchestration** that runs MCP servers per flow
5. **Enriched agents** that use MCP data in their analysis

The workflow is:
```
CSV Mapping → MCP Servers (Python) → File Paths → Claude Agents → Requirements
```

Start by running MCP servers to pre-fetch all external data, then run the requirement gathering pipeline!
