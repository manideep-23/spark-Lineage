# Step-by-Step Implementation Guide with Claude Code Commands

## Complete Implementation Checklist

This guide provides **exact commands** to build the multi-agent requirement gathering system from scratch using Claude Code CLI.

---

## Table of Contents

1. [Prerequisites & Setup](#prerequisites--setup)
2. [Phase 1: Project Structure Setup](#phase-1-project-structure-setup)
3. [Phase 2: Configuration Files](#phase-2-configuration-files)
4. [Phase 3: MCP Servers (Python)](#phase-3-mcp-servers-python)
5. [Phase 4: Shared Utilities](#phase-4-shared-utilities)
6. [Phase 5: Agent Implementation](#phase-5-agent-implementation)
7. [Phase 6: Orchestration Scripts](#phase-6-orchestration-scripts)
8. [Phase 7: Testing & Validation](#phase-7-testing--validation)
9. [Phase 8: Production Deployment](#phase-8-production-deployment)
10. [Complete Command Reference](#complete-command-reference)

---

## Prerequisites & Setup

### Step 0.1: Install Claude Code CLI

```bash
# Install Claude Code CLI
npm install -g @anthropic-ai/claude-code

# Verify installation
claude-code --version

# Set your API key
export ANTHROPIC_API_KEY="your-api-key-here"
```

### Step 0.2: Create Project Root Directory

```bash
# Create main project directory
mkdir requirement-gathering-system
cd requirement-gathering-system

# Initialize git (optional but recommended)
git init
```

---

## Phase 1: Project Structure Setup

### Step 1.1: Create Directory Structure

```bash
# Create all required directories
mkdir -p config/prompts
mkdir -p mcp-servers
mkdir -p mcp-outputs/{jira,db,hive}
mkdir -p agents
mkdir -p shared
mkdir -p utils
mkdir -p outputs
mkdir -p tests/{unit,integration,fixtures}
mkdir -p src/main/resources
mkdir -p logs

# Verify structure
tree -L 2
```

**Expected Output:**
```
requirement-gathering-system/
├── agents/
├── config/
│   └── prompts/
├── logs/
├── mcp-outputs/
│   ├── db/
│   ├── hive/
│   └── jira/
├── mcp-servers/
├── outputs/
├── shared/
├── src/
│   └── main/
│       └── resources/
├── tests/
│   ├── fixtures/
│   ├── integration/
│   └── unit/
└── utils/
```

### Step 1.2: Create Python Virtual Environment

```bash
# Create virtual environment for MCP servers
python -m venv venv

# Activate virtual environment
source venv/bin/activate  # Linux/Mac
# OR
venv\Scripts\activate  # Windows

# Upgrade pip
pip install --upgrade pip
```

---

## Phase 2: Configuration Files

### Step 2.1: Create flows-config.yaml

```bash
# Use Claude Code to create flows configuration
claude-code --prompt "Create a flows-config.yaml file with the following structure:
- flows array with 5 sample flows (user_authentication_flow, payment_processing_flow, order_fulfillment_flow, notification_flow, reporting_flow)
- Each flow should have: name, description, entry_point, enabled (boolean), priority (high/medium/low)
- Add global_settings section with: output_directory, jira_project, jira_epic_template, database_schema_prefix
- Use {FLOW_NAME} placeholder in templates
- Save to config/flows-config.yaml" \
--output config/flows-config.yaml
```

### Step 2.2: Create agent-config.yaml

```bash
# Create agent configuration
claude-code --prompt "Create an agent-config.yaml file with:
- environment: production
- mcp section with database, hive, jira configurations (use env variables)
- agents section with timeout and max_retries for each agent type (code_analysis, requirement_extraction, dependency_analysis, sequence_diagram, scenario_generation, schema_extraction, output_compiler)
- logging section with level, format, output path
- caching section with enabled, ttl, backend (redis), redis_url
- Save to config/agent-config.yaml" \
--output config/agent-config.yaml
```

### Step 2.3: Create JIRA Flow Mapping CSV

```bash
# Create JIRA-flow mapping
cat > src/main/resources/jira-flow-mapping.csv << 'EOF'
flow_name,jira_number,jira_type,priority,team
user_authentication_flow,PROJ-1234,Epic,High,Auth Team
user_authentication_flow,PROJ-1235,Story,High,Auth Team
user_authentication_flow,PROJ-1236,Story,Medium,Auth Team
payment_processing_flow,PROJ-2001,Epic,Critical,Payments Team
payment_processing_flow,PROJ-2002,Story,High,Payments Team
payment_processing_flow,PROJ-2003,Story,High,Payments Team
order_fulfillment_flow,PROJ-3001,Epic,Medium,Fulfillment Team
order_fulfillment_flow,PROJ-3002,Story,Medium,Fulfillment Team
notification_flow,PROJ-4001,Story,Low,Notifications Team
reporting_flow,PROJ-5001,Epic,Low,Analytics Team
EOF

echo "✅ JIRA mapping CSV created"
```

### Step 2.4: Create MCP Config

```bash
# Create MCP server configuration
cat > config/mcp-config.json << 'EOF'
{
  "mcpServers": {
    "database": {
      "command": "python",
      "args": ["mcp-servers/db_mcp.py"],
      "env": {
        "DB_HOST": "${DB_HOST}",
        "DB_PORT": "${DB_PORT}",
        "DB_NAME": "${DB_NAME}",
        "DB_USER": "${DB_USER}",
        "DB_PASSWORD": "${DB_PASSWORD}"
      }
    },
    "hive": {
      "command": "python",
      "args": ["mcp-servers/hive_mcp.py"],
      "env": {
        "HIVE_HOST": "${HIVE_HOST}",
        "HIVE_PORT": "${HIVE_PORT}"
      }
    },
    "jira": {
      "command": "python",
      "args": ["mcp-servers/jira_mcp.py"],
      "env": {
        "JIRA_URL": "${JIRA_URL}",
        "JIRA_EMAIL": "${JIRA_EMAIL}",
        "JIRA_API_TOKEN": "${JIRA_API_TOKEN}"
      }
    }
  }
}
EOF

echo "✅ MCP config created"
```

### Step 2.5: Create Prompt Templates

```bash
# Code Analysis Prompt
claude-code --prompt "Create a detailed prompt template for code analysis agent that:
- Analyzes flow execution path starting from {FLOW_ENTRY_POINT}
- Extracts components (controllers, services, models, utilities)
- Identifies dependencies (internal flows, external services, libraries)
- Analyzes data flow (inputs, transformations, outputs, database operations)
- Uses placeholders: {FLOW_NAME}, {FLOW_DESCRIPTION}, {FLOW_ENTRY_POINT}, {FLOW_PRIORITY}, {DB_SCHEMA_PREFIX}
- Outputs structured JSON
- Save to config/prompts/code-analysis.md" \
--output config/prompts/code-analysis.md
```

```bash
# Requirement Extraction Prompt
claude-code --prompt "Create a detailed prompt template for requirement extraction that:
- Extracts functional and non-functional requirements
- Identifies business rules and validation logic
- Uses {FLOW_NAME} and {FLOW_DESCRIPTION} placeholders
- Outputs requirements in markdown format with unique IDs like REQ-{FLOW_NAME}-001
- Includes acceptance criteria for each requirement
- Save to config/prompts/requirement-extraction.md" \
--output config/prompts/requirement-extraction.md
```

```bash
# Dependency Analysis Prompt
claude-code --prompt "Create a prompt template for dependency analysis that:
- Analyzes requirement dependencies
- Detects circular dependencies
- Determines optimal implementation order
- Groups requirements into implementation phases
- Outputs dependency graph in YAML format
- Save to config/prompts/dependency-analysis.md" \
--output config/prompts/dependency-analysis.md
```

```bash
# Sequence Diagram Prompt
claude-code --prompt "Create a prompt template for generating Mermaid sequence diagrams that:
- Shows component interactions for each requirement
- Includes happy path and error flows
- Uses Mermaid syntax
- References {FLOW_NAME}
- Save to config/prompts/sequence-diagram.md" \
--output config/prompts/sequence-diagram.md
```

```bash
# Scenario Generation Prompt
claude-code --prompt "Create a prompt template for scenario generation that:
- Creates 3-5 scenarios per requirement
- Uses Given-When-Then format
- Includes happy path and edge cases
- References {FLOW_NAME} in scenario IDs
- Save to config/prompts/scenario-generation.md" \
--output config/prompts/scenario-generation.md
```

```bash
# Schema Extraction Prompt
claude-code --prompt "Create a prompt template for schema extraction that:
- Extracts API request/response schemas
- Documents database schemas from MCP data
- Documents Hive schemas from MCP data
- Outputs in OpenAPI/JSON Schema YAML format
- Save to config/prompts/schema-extraction.md" \
--output config/prompts/schema-extraction.md
```

---

## Phase 3: MCP Servers (Python)

### Step 3.1: Create MCP Server Requirements

```bash
# Create requirements.txt for MCP servers
cat > mcp-servers/requirements.txt << 'EOF'
jira==3.5.0
psycopg2-binary==2.9.9
pyhive==0.7.0
structlog==23.2.0
python-dotenv==1.0.0
EOF

# Install dependencies
pip install -r mcp-servers/requirements.txt
```

### Step 3.2: Create JIRA MCP Server

```bash
# Use Claude Code to generate JIRA MCP server
claude-code --prompt "Create a Python JIRA MCP server (jira_mcp.py) that:
- Connects to JIRA using jira library
- Has a JiraMCP class with fetch_tickets(ticket_numbers, flow_name) method
- Fetches ticket details: summary, description, type, status, priority, assignee, acceptance_criteria, comments, attachments
- Returns structured JSON
- Handles errors gracefully
- Includes mock data fallback if JIRA unavailable
- Uses environment variables for credentials (JIRA_URL, JIRA_EMAIL, JIRA_API_TOKEN)
- Includes structlog logging
- Save to mcp-servers/jira_mcp.py" \
--output mcp-servers/jira_mcp.py
```

### Step 3.3: Create Database MCP Server

```bash
# Create Database MCP server
claude-code --prompt "Create a Python Database MCP server (db_mcp.py) that:
- Connects to PostgreSQL using psycopg2
- Has a DatabaseMCP class with extract_schema(flow_name) method
- Extracts tables by prefix (flow_name without '_flow')
- Gets detailed schema: columns, types, nullable, defaults, indexes
- Extracts foreign key relationships
- Returns structured JSON with tables and relationships
- Includes mock data fallback
- Uses environment variables for connection (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- Save to mcp-servers/db_mcp.py" \
--output mcp-servers/db_mcp.py
```

### Step 3.4: Create Hive MCP Server

```bash
# Create Hive MCP server
claude-code --prompt "Create a Python Hive MCP server (hive_mcp.py) that:
- Connects to Hive using pyhive
- Has a HiveMCP class with extract_schema(flow_name) method
- Gets Hive tables by prefix
- Extracts column details and partition information
- Returns structured JSON
- Includes mock data fallback
- Uses environment variables (HIVE_HOST, HIVE_PORT)
- Save to mcp-servers/hive_mcp.py" \
--output mcp-servers/hive_mcp.py
```

### Step 3.5: Create MCP Orchestrator

```bash
# Create main MCP orchestrator
claude-code --prompt "Create a Python MCP orchestrator (main.py) that:
- Imports JiraMCP, DatabaseMCP, HiveMCP
- Has MCPOrchestrator class with run_for_flow(flow_name, jira_numbers) method
- Runs all three MCP servers for a flow
- Writes outputs to: mcp-outputs/jira/{flow_name}/jira_content.json, mcp-outputs/db/{flow_name}/schema.json, mcp-outputs/hive/{flow_name}/schema.json
- Creates metadata.json with flow info and output paths
- Has main() with argparse for CLI: --flow-name, --jira-tickets, --output-dir
- Includes proper logging
- Save to mcp-servers/main.py" \
--output mcp-servers/main.py
```

### Step 3.6: Test MCP Servers

```bash
# Test MCP servers with mock data (no real connections needed)
python mcp-servers/main.py \
  --flow-name user_authentication_flow \
  --jira-tickets PROJ-1234 PROJ-1235 \
  --output-dir mcp-outputs

# Verify outputs
ls -la mcp-outputs/jira/user_authentication_flow/
ls -la mcp-outputs/db/user_authentication_flow/
ls -la mcp-outputs/hive/user_authentication_flow/

echo "✅ MCP servers tested"
```

---

## Phase 4: Shared Utilities

### Step 4.1: Create Template Engine

```bash
# Create template engine for {FLOW_NAME} replacement
claude-code --prompt "Create a Python TemplateEngine class (template_engine.py) that:
- Uses regex to find {PLACEHOLDER} patterns
- Has render(template, context) method to replace placeholders
- Has render_file(file_path, context) method
- Has render_config(config_dict, context) method for recursive replacement
- Save to shared/template_engine.py" \
--output shared/template_engine.py
```

### Step 4.2: Create JIRA Flow Mapper

```bash
# Create JIRA-flow mapper
claude-code --prompt "Create a Python JiraFlowMapper class (jira_flow_mapper.py) that:
- Reads jira-flow-mapping.csv from src/main/resources/
- Has get_jira_tickets(flow_name) method returning list of ticket dicts
- Has get_jira_numbers(flow_name) method returning list of ticket IDs
- Has has_jira_tickets(flow_name) boolean method
- Has get_stats(flow_name) method for ticket statistics
- Includes structlog logging
- Save to shared/jira_flow_mapper.py" \
--output shared/jira_flow_mapper.py
```

### Step 4.3: Create Flow Context

```bash
# Create flow context manager
claude-code --prompt "Create a Python FlowContext class (flow_context.py) that:
- Initializes with flow_config and global_settings
- Builds context dict with all placeholders: FLOW_NAME, FLOW_DESCRIPTION, FLOW_ENTRY_POINT, OUTPUT_DIR, JIRA_EPIC, DB_SCHEMA_PREFIX, etc.
- Has _setup_directories() to create output directories
- Has get(key, default), get_all(), set(key, value), update(dict) methods
- Has save_context() to write context to JSON file
- Save to shared/flow_context.py" \
--output shared/flow_context.py
```

### Step 4.4: Create MCP Client

```bash
# Create MCP client to read outputs
claude-code --prompt "Create a Python MCPClient class (mcp_client.py) that:
- Reads from mcp-outputs directory
- Has get_jira_data(flow_name) to read jira_content.json
- Has get_db_schema(flow_name) to read db schema.json
- Has get_hive_schema(flow_name) to read hive schema.json
- Has get_all_mcp_data(flow_name) to get all data
- Returns None if files not found, logs warnings
- Save to shared/mcp_client.py" \
--output shared/mcp_client.py
```

### Step 4.5: Create Flow Config Loader

```bash
# Create config loader
claude-code --prompt "Create a Python FlowConfigLoader class (config_loader.py) that:
- Has FlowConfig dataclass with name, description, entry_point, enabled, priority, custom_settings
- Loads from config/flows-config.yaml
- Has get_all_flows(), get_enabled_flows(), get_flows_by_priority(priority), get_flow_by_name(name), get_sorted_flows() methods
- Save to utils/config_loader.py" \
--output utils/config_loader.py
```

### Step 4.6: Create Logger Utility

```bash
# Create structured logger
claude-code --prompt "Create a Python logger utility (logger.py) that:
- Configures structlog with JSON output
- Has setup_logging(log_level, log_file) function
- Returns configured logger
- Save to utils/logger.py" \
--output utils/logger.py
```

---

## Phase 5: Agent Implementation

### Step 5.1: Create Base Agent

```bash
# Create base agent class
claude-code --prompt "Create a Python BaseAgent abstract class (base_agent.py) that:
- Takes config and flow_context in __init__
- Has template_engine instance
- Has get_prompt_template(template_name) that loads and renders prompts with flow context
- Has get_output_path(filename) for flow-specific output paths
- Has log(message, **kwargs) with flow context
- Has abstract execute(inputs) method
- Has run(inputs) wrapper with logging and error handling
- Uses structlog
- Save to agents/base_agent.py" \
--output agents/base_agent.py
```

### Step 5.2: Create Code Analysis Agent

```bash
# Create code analysis agent
claude-code --prompt "Create a Python CodeAnalysisAgent class (code_analysis_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives codebase_path and mcp_data
- Loads code-analysis.md prompt template
- Enhances prompt with MCP context (DB tables, Hive tables, JIRA tickets)
- Uses Claude Code CLI to analyze codebase
- Returns structured analysis with flow_name and entry_point
- Save to agents/code_analysis_agent.py

Include actual subprocess call to claude-code CLI with:
- --prompt (enhanced prompt file)
- --input (codebase path)
- --output (flow-specific output path)
" \
--output agents/code_analysis_agent.py
```

### Step 5.3: Create Requirement Extraction Agent

```bash
# Create requirement extraction agent
claude-code --prompt "Create a Python RequirementExtractionAgent class (requirement_extraction_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives code_analysis and jira_data
- Loads requirement-extraction.md prompt template
- Enriches with JIRA ticket descriptions and acceptance criteria
- Uses Claude Code to extract requirements
- Ensures requirement IDs use format REQ-{FLOW_NAME}-NNN
- Returns structured requirements list
- Save to agents/requirement_extraction_agent.py" \
--output agents/requirement_extraction_agent.py
```

### Step 5.4: Create Dependency Analysis Agent

```bash
# Create dependency analysis agent
claude-code --prompt "Create a Python DependencyAnalysisAgent class (dependency_analysis_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives requirements and code_analysis
- Analyzes requirement dependencies
- Detects circular dependencies
- Determines implementation order
- Returns dependency graph in YAML format
- Save to agents/dependency_analysis_agent.py" \
--output agents/dependency_analysis_agent.py
```

### Step 5.5: Create Sequence Diagram Agent

```bash
# Create sequence diagram agent
claude-code --prompt "Create a Python SequenceDiagramAgent class (sequence_diagram_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives requirements and code_analysis
- Generates Mermaid sequence diagrams for each requirement
- Saves diagrams to flow-specific diagrams directory
- Returns paths to generated diagrams
- Save to agents/sequence_diagram_agent.py" \
--output agents/sequence_diagram_agent.py
```

### Step 5.6: Create Scenario Generation Agent

```bash
# Create scenario generation agent
claude-code --prompt "Create a Python ScenarioGenerationAgent class (scenario_generation_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives requirements and dependencies
- Generates 3-5 scenarios per requirement in Given-When-Then format
- Includes happy path and edge cases
- Saves to flow-specific scenarios directory
- Save to agents/scenario_generation_agent.py" \
--output agents/scenario_generation_agent.py
```

### Step 5.7: Create Schema Extraction Agent

```bash
# Create schema extraction agent
claude-code --prompt "Create a Python SchemaExtractionAgent class (schema_extraction_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives code_analysis and mcp_connections list
- Uses MCP data from DB and Hive
- Extracts API schemas from code
- Outputs OpenAPI/JSON Schema YAML
- Saves to flow-specific schemas directory
- Save to agents/schema_extraction_agent.py" \
--output agents/schema_extraction_agent.py
```

### Step 5.8: Create Output Compiler Agent

```bash
# Create output compiler agent
claude-code --prompt "Create a Python OutputCompilerAgent class (output_compiler_agent.py) that:
- Extends BaseAgent
- execute(inputs) receives all previous agent outputs
- Compiles comprehensive markdown report
- Creates traceability matrix
- Saves final report to flow output directory
- Returns path to final report
- Save to agents/output_compiler_agent.py" \
--output agents/output_compiler_agent.py
```

---

## Phase 6: Orchestration Scripts

### Step 6.1: Create Flow Orchestrator

```bash
# Create flow orchestrator
claude-code --prompt "Create a Python FlowOrchestrator class (flow_orchestrator.py) that:
- Initializes all agents with flow_context
- Has run_full_pipeline(codebase_path) method
- Runs MCP servers first using _run_mcp_servers()
- Executes agents sequentially: code_analysis -> requirement_extraction -> dependency_analysis -> (parallel: sequence_diagram, scenario_generation, schema_extraction) -> output_compiler
- Includes _sync_to_jira() method to create JIRA epic and stories
- Returns complete results dict
- Includes comprehensive error handling and logging
- Save to agents/flow_orchestrator.py" \
--output agents/flow_orchestrator.py
```

### Step 6.2: Create MCP Server Runner

```bash
# Create MCP server runner script
claude-code --prompt "Create a Python script (run_mcp_servers.py) that:
- Uses FlowConfigLoader and JiraFlowMapper
- Has run_mcp_for_flow(flow_name, jira_tickets) function
- main() with argparse: --parallel, --flows
- Runs MCP servers for all enabled flows (or specified flows)
- Supports parallel execution with ThreadPoolExecutor
- Prints summary of successful/failed runs
- Save to run_mcp_servers.py" \
--output run_mcp_servers.py
```

### Step 6.3: Create Single Flow Runner

```bash
# Create single flow runner
claude-code --prompt "Create a Python script (run.py) that:
- Uses FlowConfigLoader and FlowOrchestrator
- argparse with: --flow-name (required), --codebase (required), --config
- Validates flow exists and is enabled
- Creates FlowContext
- Runs FlowOrchestrator.run_full_pipeline()
- Prints success message with output directory and stats
- Includes error handling
- Save to run.py" \
--output run.py
```

### Step 6.4: Create Multi-Flow Runner

```bash
# Create multi-flow runner
claude-code --prompt "Create a Python MultiFlowOrchestrator class and script (run_all_flows.py) that:
- Loads all enabled flows
- Has run_all_flows(codebase_path, parallel) method
- Supports sequential and parallel execution
- Generates summary report (multi_flow_summary.md)
- argparse: --codebase, --parallel, --config
- Shows progress and final statistics
- Save to run_all_flows.py" \
--output run_all_flows.py
```

### Step 6.5: Create Batch Flow Runner

```bash
# Create batch flow runner
claude-code --prompt "Create a Python script (run_flow_batch.py) that:
- argparse: --flows (list), --codebase, --parallel, --priority
- Filters flows by name or priority
- Uses MultiFlowOrchestrator
- Prints flow list before execution
- Shows completion summary
- Save to run_flow_batch.py" \
--output run_flow_batch.py
```

---

## Phase 7: Testing & Validation

### Step 7.1: Create Test Fixtures

```bash
# Create sample codebase structure for testing
mkdir -p tests/fixtures/sample-codebase/src/{auth,payments,orders}

# Create sample Python files
cat > tests/fixtures/sample-codebase/src/auth/login.py << 'EOF'
"""User authentication module"""

def authenticate_user(email, password):
    """Authenticate user with email and password"""
    # OAuth2 implementation
    pass

def create_session(user_id):
    """Create user session"""
    pass
EOF

cat > tests/fixtures/sample-codebase/src/payments/processor.py << 'EOF'
"""Payment processing module"""

def process_payment(amount, payment_method):
    """Process payment transaction"""
    # Payment gateway integration
    pass
EOF

echo "✅ Test fixtures created"
```

### Step 7.2: Create Unit Tests

```bash
# Test template engine
claude-code --prompt "Create pytest unit tests (test_template_engine.py) for TemplateEngine that:
- Tests render() with simple placeholders
- Tests render() with multiple placeholders
- Tests render_config() with nested dicts
- Tests missing placeholders are kept unchanged
- Save to tests/unit/test_template_engine.py" \
--output tests/unit/test_template_engine.py
```

```bash
# Test JIRA mapper
claude-code --prompt "Create pytest unit tests (test_jira_flow_mapper.py) for JiraFlowMapper that:
- Tests loading CSV file
- Tests get_jira_tickets() returns correct tickets
- Tests get_jira_numbers() returns IDs only
- Tests has_jira_tickets() boolean
- Tests get_stats() aggregation
- Save to tests/unit/test_jira_flow_mapper.py" \
--output tests/unit/test_jira_flow_mapper.py
```

```bash
# Test flow context
claude-code --prompt "Create pytest unit tests (test_flow_context.py) for FlowContext that:
- Tests context building with placeholders
- Tests directory creation
- Tests get/set/update methods
- Tests save_context() JSON output
- Save to tests/unit/test_flow_context.py" \
--output tests/unit/test_flow_context.py
```

### Step 7.3: Create Integration Tests

```bash
# Create integration test
claude-code --prompt "Create pytest integration test (test_full_pipeline.py) that:
- Tests complete workflow from MCP to final output
- Uses test fixtures/sample-codebase
- Validates all expected outputs are created
- Checks requirement format and IDs
- Validates dependency graph structure
- Save to tests/integration/test_full_pipeline.py" \
--output tests/integration/test_full_pipeline.py
```

### Step 7.4: Run Tests

```bash
# Install pytest
pip install pytest pytest-cov

# Run unit tests
pytest tests/unit/ -v

# Run integration tests
pytest tests/integration/ -v

# Run all tests with coverage
pytest tests/ -v --cov=agents --cov=shared --cov=utils --cov-report=html

echo "✅ Tests completed"
```

---

## Phase 8: Production Deployment

### Step 8.1: Create Environment Setup Script

```bash
# Create .env.example
cat > .env.example << 'EOF'
# JIRA Configuration
JIRA_URL=https://your-company.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=your-jira-api-token

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mydb
DB_USER=dbuser
DB_PASSWORD=dbpassword

# Hive Configuration
HIVE_HOST=localhost
HIVE_PORT=10000

# Anthropic API
ANTHROPIC_API_KEY=your-anthropic-api-key

# Application Settings
LOG_LEVEL=INFO
OUTPUT_BASE_DIR=outputs
MCP_OUTPUT_DIR=mcp-outputs
EOF

echo "✅ Environment template created"
echo "⚠️  Copy .env.example to .env and fill in your credentials"
```

### Step 8.2: Create Requirements File

```bash
# Create main requirements.txt
cat > requirements.txt << 'EOF'
# Core dependencies
pyyaml==6.0.1
structlog==23.2.0
python-dotenv==1.0.0

# MCP server dependencies
jira==3.5.0
psycopg2-binary==2.9.9
pyhive==0.7.0

# Testing dependencies
pytest==7.4.3
pytest-cov==4.1.0

# Optional: Redis for caching
redis==5.0.1

# Optional: Advanced logging
colorlog==6.8.0
EOF

pip install -r requirements.txt
```

### Step 8.3: Create README

```bash
# Generate README
claude-code --prompt "Create a comprehensive README.md that includes:
- Project overview and architecture
- Prerequisites and installation instructions
- Quick start guide
- Configuration guide (environment variables, YAML files, CSV mapping)
- Usage examples for all scripts (run.py, run_all_flows.py, run_mcp_servers.py)
- Troubleshooting section
- Contributing guidelines
- License (MIT)
- Save to README.md" \
--output README.md
```

### Step 8.4: Create Docker Support (Optional)

```bash
# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Install Claude Code CLI
RUN npm install -g @anthropic-ai/claude-code

# Copy application code
COPY . .

# Create output directories
RUN mkdir -p mcp-outputs outputs logs

CMD ["python", "run_all_flows.py", "--help"]
EOF

# Create docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  requirement-gatherer:
    build: .
    volumes:
      - ./mcp-outputs:/app/mcp-outputs
      - ./outputs:/app/outputs
      - ./logs:/app/logs
      - ${CODEBASE_PATH}:/app/codebase:ro
    env_file:
      - .env
    command: python run_all_flows.py --codebase /app/codebase --parallel
EOF

echo "✅ Docker files created"
```

### Step 8.5: Create Makefile for Common Tasks

```bash
# Create Makefile
cat > Makefile << 'EOF'
.PHONY: help setup test run-mcp run-single run-all clean

help:
	@echo "Available commands:"
	@echo "  make setup      - Install dependencies and setup environment"
	@echo "  make test       - Run all tests"
	@echo "  make run-mcp    - Run MCP servers for all flows"
	@echo "  make run-single - Run single flow (specify FLOW=flow_name CODEBASE=/path)"
	@echo "  make run-all    - Run all flows (specify CODEBASE=/path)"
	@echo "  make clean      - Clean generated outputs"

setup:
	python -m venv venv
	. venv/bin/activate && pip install -r requirements.txt
	cp .env.example .env
	@echo "✅ Setup complete. Edit .env with your credentials."

test:
	pytest tests/ -v --cov=agents --cov=shared --cov=utils

run-mcp:
	python run_mcp_servers.py --parallel

run-single:
	python run.py --flow-name $(FLOW) --codebase $(CODEBASE)

run-all:
	python run_all_flows.py --codebase $(CODEBASE) --parallel

clean:
	rm -rf mcp-outputs/* outputs/* logs/*
	find . -type d -name __pycache__ -exec rm -rf {} +
	find . -type f -name "*.pyc" -delete

.DEFAULT_GOAL := help
EOF

echo "✅ Makefile created"
```

---

## Complete Command Reference

### Quick Start Commands (Copy-Paste Ready)

```bash
# ============================================
# COMPLETE SETUP FROM SCRATCH
# ============================================

# 1. Clone/Create project
mkdir requirement-gathering-system && cd requirement-gathering-system

# 2. Install Claude Code CLI
npm install -g @anthropic-ai/claude-code

# 3. Set API key
export ANTHROPIC_API_KEY="your-key-here"

# 4. Create all directories
mkdir -p config/prompts mcp-servers mcp-outputs/{jira,db,hive} agents shared utils outputs tests/{unit,integration,fixtures} src/main/resources logs

# 5. Create Python virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate    # Windows

# 6. Install base dependencies
pip install pyyaml structlog python-dotenv jira psycopg2-binary pyhive pytest

# ============================================
# CONFIGURATION FILES
# ============================================

# 7. Create flows configuration (using the earlier command)
claude-code --prompt "Create flows-config.yaml with 5 sample flows..." --output config/flows-config.yaml

# 8. Create agent configuration
claude-code --prompt "Create agent-config.yaml..." --output config/agent-config.yaml

# 9. Create JIRA mapping CSV
cat > src/main/resources/jira-flow-mapping.csv << 'EOF'
flow_name,jira_number,jira_type,priority,team
user_authentication_flow,PROJ-1234,Epic,High,Auth Team
payment_processing_flow,PROJ-2001,Epic,Critical,Payments Team
EOF

# 10. Create all prompt templates
claude-code --prompt "Create code-analysis prompt..." --output config/prompts/code-analysis.md
claude-code --prompt "Create requirement-extraction prompt..." --output config/prompts/requirement-extraction.md
claude-code --prompt "Create dependency-analysis prompt..." --output config/prompts/dependency-analysis.md
claude-code --prompt "Create sequence-diagram prompt..." --output config/prompts/sequence-diagram.md
claude-code --prompt "Create scenario-generation prompt..." --output config/prompts/scenario-generation.md
claude-code --prompt "Create schema-extraction prompt..." --output config/prompts/schema-extraction.md

# ============================================
# MCP SERVERS
# ============================================

# 11. Create MCP servers
claude-code --prompt "Create jira_mcp.py..." --output mcp-servers/jira_mcp.py
claude-code --prompt "Create db_mcp.py..." --output mcp-servers/db_mcp.py
claude-code --prompt "Create hive_mcp.py..." --output mcp-servers/hive_mcp.py
claude-code --prompt "Create main.py MCP orchestrator..." --output mcp-servers/main.py

# ============================================
# SHARED UTILITIES
# ============================================

# 12. Create shared utilities
claude-code --prompt "Create template_engine.py..." --output shared/template_engine.py
claude-code --prompt "Create jira_flow_mapper.py..." --output shared/jira_flow_mapper.py
claude-code --prompt "Create flow_context.py..." --output shared/flow_context.py
claude-code --prompt "Create mcp_client.py..." --output shared/mcp_client.py

# 13. Create utils
claude-code --prompt "Create config_loader.py..." --output utils/config_loader.py
claude-code --prompt "Create logger.py..." --output utils/logger.py

# ============================================
# AGENTS
# ============================================

# 14. Create all agents
claude-code --prompt "Create base_agent.py..." --output agents/base_agent.py
claude-code --prompt "Create code_analysis_agent.py..." --output agents/code_analysis_agent.py
claude-code --prompt "Create requirement_extraction_agent.py..." --output agents/requirement_extraction_agent.py
claude-code --prompt "Create dependency_analysis_agent.py..." --output agents/dependency_analysis_agent.py
claude-code --prompt "Create sequence_diagram_agent.py..." --output agents/sequence_diagram_agent.py
claude-code --prompt "Create scenario_generation_agent.py..." --output agents/scenario_generation_agent.py
claude-code --prompt "Create schema_extraction_agent.py..." --output agents/schema_extraction_agent.py
claude-code --prompt "Create output_compiler_agent.py..." --output agents/output_compiler_agent.py

# ============================================
# ORCHESTRATORS
# ============================================

# 15. Create orchestration scripts
claude-code --prompt "Create flow_orchestrator.py..." --output agents/flow_orchestrator.py
claude-code --prompt "Create run_mcp_servers.py..." --output run_mcp_servers.py
claude-code --prompt "Create run.py..." --output run.py
claude-code --prompt "Create run_all_flows.py..." --output run_all_flows.py
claude-code --prompt "Create run_flow_batch.py..." --output run_flow_batch.py

# ============================================
# TESTING
# ============================================

# 16. Create tests
claude-code --prompt "Create test_template_engine.py..." --output tests/unit/test_template_engine.py
claude-code --prompt "Create test_jira_flow_mapper.py..." --output tests/unit/test_jira_flow_mapper.py
claude-code --prompt "Create test_flow_context.py..." --output tests/unit/test_flow_context.py
claude-code --prompt "Create test_full_pipeline.py..." --output tests/integration/test_full_pipeline.py

# 17. Run tests
pytest tests/ -v

# ============================================
# ENVIRONMENT SETUP
# ============================================

# 18. Create .env file
cat > .env << 'EOF'
JIRA_URL=https://your-company.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=your-token
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mydb
DB_USER=user
DB_PASSWORD=pass
HIVE_HOST=localhost
HIVE_PORT=10000
ANTHROPIC_API_KEY=your-key
EOF

# ============================================
# EXECUTION
# ============================================

# 19. Test MCP servers
python mcp-servers/main.py \
  --flow-name user_authentication_flow \
  --jira-tickets PROJ-1234 PROJ-1235

# 20. Run MCP servers for all flows
python run_mcp_servers.py --parallel

# 21. Run single flow
python run.py \
  --flow-name user_authentication_flow \
  --codebase /path/to/your/codebase

# 22. Run all flows
python run_all_flows.py \
  --codebase /path/to/your/codebase \
  --parallel

# 23. Run specific flows
python run_flow_batch.py \
  --flows user_authentication_flow payment_processing_flow \
  --codebase /path/to/your/codebase \
  --parallel

# 24. Run flows by priority
python run_flow_batch.py \
  --priority high \
  --codebase /path/to/your/codebase

# ============================================
# VERIFICATION
# ============================================

# 25. Check outputs
tree outputs/
tree mcp-outputs/

# 26. View results
cat outputs/user_authentication_flow/final_report.md
cat outputs/multi_flow_summary.md

# 27. Check logs
tail -f logs/requirement-gathering.log
```

---

## Execution Workflow Summary

### Phase 1: Pre-Execution (Run Once)

```bash
# Step 1: Run MCP servers to gather all external data
python run_mcp_servers.py --parallel

# This creates:
# mcp-outputs/
#   ├── jira/{flow_name}/jira_content.json
#   ├── db/{flow_name}/schema.json
#   └── hive/{flow_name}/schema.json
```

### Phase 2: Main Execution (Run Per Analysis)

```bash
# Option A: Single flow
python run.py \
  --flow-name user_authentication_flow \
  --codebase /path/to/repo

# Option B: All flows (sequential)
python run_all_flows.py \
  --codebase /path/to/repo

# Option C: All flows (parallel - RECOMMENDED)
python run_all_flows.py \
  --codebase /path/to/repo \
  --parallel

# Option D: Selected flows
python run_flow_batch.py \
  --flows flow1 flow2 flow3 \
  --codebase /path/to/repo \
  --parallel

# Option E: By priority
python run_flow_batch.py \
  --priority high \
  --codebase /path/to/repo
```

### Phase 3: Post-Execution (Review & Deploy)

```bash
# View summary
cat outputs/multi_flow_summary.md

# View individual flow reports
cat outputs/user_authentication_flow/final_report.md

# Check JIRA sync
# (Login to JIRA and verify epics/stories were created)

# Export for documentation
cp outputs/*/final_report.md /path/to/docs/
```

---

## Troubleshooting Commands

### Debug MCP Servers

```bash
# Test individual MCP server
python -c "
from mcp_servers.jira_mcp import JiraMCP
jira = JiraMCP()
result = jira.fetch_tickets(['PROJ-1234'], 'test_flow')
print(result)
"

# Check MCP outputs
ls -la mcp-outputs/jira/*/
cat mcp-outputs/jira/user_authentication_flow/jira_content.json | python -m json.tool
```

### Debug Agents

```bash
# Test template engine
python -c "
from shared.template_engine import TemplateEngine
engine = TemplateEngine()
result = engine.render('Hello {FLOW_NAME}', {'FLOW_NAME': 'test'})
print(result)
"

# Test JIRA mapper
python -c "
from shared.jira_flow_mapper import JiraFlowMapper
mapper = JiraFlowMapper()
tickets = mapper.get_jira_tickets('user_authentication_flow')
print(f'Found {len(tickets)} tickets')
"

# Test flow context
python -c "
from utils.config_loader import FlowConfigLoader
from shared.flow_context import FlowContext

loader = FlowConfigLoader()
flow = loader.get_flow_by_name('user_authentication_flow')
context = FlowContext(flow, loader.global_settings)
print(context.get_all())
"
```

### Check Logs

```bash
# View real-time logs
tail -f logs/requirement-gathering.log

# View flow-specific logs
tail -f outputs/user_authentication_flow/logs/execution.log

# Search for errors
grep -r "ERROR" logs/
grep -r "ERROR" outputs/*/logs/
```

### Validate Outputs

```bash
# Check if all expected files exist
python -c "
from pathlib import Path
flow_name = 'user_authentication_flow'
expected_files = [
    f'outputs/{flow_name}/requirements/requirements.md',
    f'outputs/{flow_name}/diagrams/',
    f'outputs/{flow_name}/schemas/',
    f'outputs/{flow_name}/scenarios/',
    f'outputs/{flow_name}/final_report.md',
]
for file in expected_files:
    exists = Path(file).exists()
    print(f'{'✅' if exists else '❌'} {file}')
"
```

---

## Performance Optimization Commands

### Parallel Execution

```bash
# Run with custom worker count
python run_all_flows.py \
  --codebase /path/to/repo \
  --parallel \
  --max-workers 5  # Adjust based on CPU cores
```

### Caching

```bash
# Enable Redis caching (optional)
docker run -d -p 6379:6379 redis

# Configure in agent-config.yaml
# caching:
#   enabled: true
#   backend: redis
#   redis_url: redis://localhost:6379
```

### Incremental Analysis

```bash
# Only analyze changed flows (custom script)
python run_incremental.py \
  --since last-run \
  --codebase /path/to/repo
```

---

## CI/CD Integration

### GitHub Actions Example

```bash
# Create .github/workflows/requirements.yml
mkdir -p .github/workflows

cat > .github/workflows/requirements.yml << 'EOF'
name: Requirement Gathering

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  gather-requirements:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'
      
      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          npm install -g @anthropic-ai/claude-code
      
      - name: Run MCP servers
        env:
          JIRA_URL: ${{ secrets.JIRA_URL }}
          JIRA_EMAIL: ${{ secrets.JIRA_EMAIL }}
          JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
          DB_HOST: ${{ secrets.DB_HOST }}
          DB_USER: ${{ secrets.DB_USER }}
          DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
        run: python run_mcp_servers.py --parallel
      
      - name: Run requirement gathering
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: python run_all_flows.py --codebase . --parallel
      
      - name: Upload results
        uses: actions/upload-artifact@v3
        with:
          name: requirements
          path: outputs/
EOF
```

---

## Final Checklist

Before running in production, verify:

```bash
# ✅ All configuration files created
ls config/flows-config.yaml
ls config/agent-config.yaml
ls src/main/resources/jira-flow-mapping.csv

# ✅ All prompt templates created
ls config/prompts/*.md

# ✅ All MCP servers created
ls mcp-servers/*.py

# ✅ All shared utilities created
ls shared/*.py

# ✅ All utils created
ls utils/*.py

# ✅ All agents created
ls agents/*.py

# ✅ All orchestrators created
ls run*.py

# ✅ Environment configured
test -f .env && echo "✅ .env exists" || echo "❌ .env missing"

# ✅ Dependencies installed
python -c "import yaml, structlog, jira" && echo "✅ Dependencies OK" || echo "❌ Missing dependencies"

# ✅ Tests passing
pytest tests/ -v

# ✅ MCP servers working
python mcp-servers/main.py --flow-name test_flow --jira-tickets PROJ-1234

# 🚀 Ready to run!
echo "✅ System ready for production!"
```

---

## Quick Reference Card

```bash
# ═══════════════════════════════════════════════════════════
#                    QUICK COMMAND REFERENCE
# ═══════════════════════════════════════════════════════════

# SETUP (Run once)
make setup                                    # Full environment setup
python run_mcp_servers.py --parallel         # Fetch all external data

# EXECUTION
python run.py --flow-name FLOW --codebase PATH              # Single flow
python run_all_flows.py --codebase PATH --parallel          # All flows
python run_flow_batch.py --flows F1 F2 --codebase PATH     # Selected flows
python run_flow_batch.py --priority high --codebase PATH   # By priority

# TESTING
pytest tests/ -v                              # Run all tests
make test                                     # Run tests via Makefile

# DEBUGGING
python -m pdb run.py --flow-name FLOW --codebase PATH      # Debug single flow
tail -f logs/*.log                            # Monitor logs
cat outputs/FLOW/final_report.md             # View results

# CLEANUP
make clean                                    # Clean all outputs
rm -rf mcp-outputs/* outputs/*               # Manual cleanup

# ═══════════════════════════════════════════════════════════
```

---

## Success! 🎉

You now have:
- ✅ Complete multi-agent requirement gathering system
- ✅ Python MCP servers for JIRA, DB, and Hive
- ✅ Dynamic flow configuration with CSV mapping
- ✅ All agents implemented with Claude Code
- ✅ Parallel execution capability
- ✅ Comprehensive testing suite
- ✅ Production-ready deployment scripts

**Next Steps:**
1. Configure `.env` with your credentials
2. Update `jira-flow-mapping.csv` with your actual JIRA tickets
3. Run `make setup` to initialize
4. Test with `python run.py --flow-name user_authentication_flow --codebase /your/repo`
5. Scale to all flows with `python run_all_flows.py --codebase /your/repo --parallel`

Happy requirement gathering! 🚀
