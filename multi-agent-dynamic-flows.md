# Multi-Agent System with Dynamic Flow Configuration

## Overview

This guide extends the base multi-agent architecture to handle **dynamic flow processing** where `FLOW_NAME` is replaced dynamically for each flow in your configuration file.

---

## Configuration File Structure

### flows-config.yaml

```yaml
# flows-config.yaml - Central configuration for all flows

flows:
  - name: user_authentication_flow
    description: "User login and authentication process"
    entry_point: "src/auth/login.py"
    enabled: true
    priority: high
    
  - name: payment_processing_flow
    description: "Payment gateway integration"
    entry_point: "src/payments/processor.py"
    enabled: true
    priority: high
    
  - name: order_fulfillment_flow
    description: "Order processing and fulfillment"
    entry_point: "src/orders/fulfillment.py"
    enabled: true
    priority: medium
    
  - name: notification_flow
    description: "Email and SMS notifications"
    entry_point: "src/notifications/sender.py"
    enabled: true
    priority: low
    
  - name: reporting_flow
    description: "Data aggregation and reporting"
    entry_point: "src/reports/generator.py"
    enabled: false
    priority: low

global_settings:
  output_directory: "outputs/{FLOW_NAME}"
  jira_project: "PROJ"
  jira_epic_template: "Requirements - {FLOW_NAME}"
  database_schema_prefix: "{FLOW_NAME}_"
```

---

## Enhanced File Structure

```
requirement-gathering-system/
│
├── config/
│   ├── flows-config.yaml           # Master flow configuration
│   ├── mcp-config.json              # MCP server configurations
│   ├── agent-config.yaml            # Agent-specific settings
│   └── prompts/
│       ├── code-analysis.md
│       ├── requirement-extraction.md
│       └── ... (with {FLOW_NAME} placeholders)
│
├── agents/
│   ├── base_agent.py
│   ├── orchestrator.py
│   ├── flow_orchestrator.py         # NEW: Multi-flow orchestrator
│   ├── code_analysis_agent.py
│   ├── requirement_extraction_agent.py
│   ├── dependency_analysis_agent.py
│   ├── sequence_diagram_agent.py
│   ├── scenario_generation_agent.py
│   ├── schema_extraction_agent.py
│   └── output_compiler_agent.py
│
├── shared/
│   ├── state_manager.py
│   ├── flow_context.py              # NEW: Flow-specific context
│   ├── template_engine.py           # NEW: Dynamic template replacement
│   ├── mcp_client.py
│   ├── cache_layer.py
│   └── validators.py
│
├── utils/
│   ├── logger.py
│   ├── file_handler.py
│   ├── config_loader.py             # NEW: Flow config loader
│   └── error_handler.py
│
├── outputs/
│   ├── user_authentication_flow/
│   ├── payment_processing_flow/
│   └── ... (one directory per flow)
│
├── run.py                            # Single flow execution
├── run_all_flows.py                  # NEW: All flows execution
└── run_flow_batch.py                 # NEW: Batch flow execution
```

---

## Implementation: Dynamic Flow Processing

### 1. Flow Configuration Loader

```python
# utils/config_loader.py

import yaml
from pathlib import Path
from typing import List, Dict, Optional

class FlowConfig:
    """Represents a single flow configuration"""
    def __init__(self, data: Dict):
        self.name = data['name']
        self.description = data.get('description', '')
        self.entry_point = data.get('entry_point', '')
        self.enabled = data.get('enabled', True)
        self.priority = data.get('priority', 'medium')
        self.custom_settings = data.get('custom_settings', {})
        
    def __repr__(self):
        return f"FlowConfig(name={self.name}, enabled={self.enabled})"

class FlowConfigLoader:
    """Loads and manages flow configurations"""
    
    def __init__(self, config_path: str = 'config/flows-config.yaml'):
        self.config_path = Path(config_path)
        self.flows: List[FlowConfig] = []
        self.global_settings: Dict = {}
        self._load_config()
        
    def _load_config(self):
        """Load configuration from YAML file"""
        with open(self.config_path, 'r') as f:
            config = yaml.safe_load(f)
            
        self.flows = [FlowConfig(flow) for flow in config.get('flows', [])]
        self.global_settings = config.get('global_settings', {})
        
    def get_all_flows(self) -> List[FlowConfig]:
        """Get all flows"""
        return self.flows
        
    def get_enabled_flows(self) -> List[FlowConfig]:
        """Get only enabled flows"""
        return [flow for flow in self.flows if flow.enabled]
        
    def get_flows_by_priority(self, priority: str) -> List[FlowConfig]:
        """Get flows filtered by priority"""
        return [flow for flow in self.flows if flow.priority == priority]
        
    def get_flow_by_name(self, name: str) -> Optional[FlowConfig]:
        """Get specific flow by name"""
        for flow in self.flows:
            if flow.name == name:
                return flow
        return None
        
    def get_sorted_flows(self) -> List[FlowConfig]:
        """Get flows sorted by priority (high -> medium -> low)"""
        priority_order = {'high': 0, 'medium': 1, 'low': 2}
        return sorted(
            self.get_enabled_flows(),
            key=lambda f: priority_order.get(f.priority, 999)
        )

# Example usage
loader = FlowConfigLoader()
enabled_flows = loader.get_enabled_flows()
print(f"Found {len(enabled_flows)} enabled flows")
```

---

### 2. Template Engine for Dynamic Replacement

```python
# shared/template_engine.py

import re
from typing import Dict, Any
from pathlib import Path

class TemplateEngine:
    """Handles dynamic replacement of {FLOW_NAME} and other placeholders"""
    
    def __init__(self):
        self.placeholder_pattern = re.compile(r'\{([A-Z_]+)\}')
        
    def render(self, template: str, context: Dict[str, Any]) -> str:
        """
        Replace all placeholders in template with context values
        
        Args:
            template: String with {PLACEHOLDER} markers
            context: Dictionary with placeholder values
            
        Returns:
            Rendered string with all placeholders replaced
        """
        def replacer(match):
            key = match.group(1)
            value = context.get(key, match.group(0))  # Keep original if not found
            return str(value)
            
        return self.placeholder_pattern.sub(replacer, template)
        
    def render_file(self, file_path: str, context: Dict[str, Any]) -> str:
        """
        Load file and replace all placeholders
        
        Args:
            file_path: Path to template file
            context: Dictionary with placeholder values
            
        Returns:
            Rendered content
        """
        with open(file_path, 'r') as f:
            template = f.read()
        return self.render(template, context)
        
    def render_config(self, config: Dict, context: Dict[str, Any]) -> Dict:
        """
        Recursively replace placeholders in configuration dictionary
        
        Args:
            config: Configuration dictionary
            context: Dictionary with placeholder values
            
        Returns:
            Configuration with all placeholders replaced
        """
        if isinstance(config, dict):
            return {k: self.render_config(v, context) for k, v in config.items()}
        elif isinstance(config, list):
            return [self.render_config(item, context) for item in config]
        elif isinstance(config, str):
            return self.render(config, context)
        else:
            return config

# Example usage
engine = TemplateEngine()
template = "Processing flow: {FLOW_NAME} in directory: outputs/{FLOW_NAME}"
context = {'FLOW_NAME': 'user_authentication_flow'}
result = engine.render(template, context)
# Output: "Processing flow: user_authentication_flow in directory: outputs/user_authentication_flow"
```

---

### 3. Flow Context Manager

```python
# shared/flow_context.py

from typing import Dict, Any, Optional
from pathlib import Path
import json

class FlowContext:
    """
    Manages context for a single flow execution
    Provides dynamic placeholder values for all agents
    """
    
    def __init__(self, flow_config, global_settings: Dict):
        self.flow_name = flow_config.name
        self.flow_config = flow_config
        self.global_settings = global_settings
        
        # Initialize context variables
        self._context = self._build_context()
        
        # Create flow-specific directories
        self._setup_directories()
        
    def _build_context(self) -> Dict[str, Any]:
        """Build the complete context dictionary"""
        return {
            'FLOW_NAME': self.flow_name,
            'FLOW_DESCRIPTION': self.flow_config.description,
            'FLOW_ENTRY_POINT': self.flow_config.entry_point,
            'FLOW_PRIORITY': self.flow_config.priority,
            'OUTPUT_DIR': f"outputs/{self.flow_name}",
            'JIRA_EPIC': f"Requirements - {self.flow_name}",
            'JIRA_PROJECT': self.global_settings.get('jira_project', 'PROJ'),
            'DB_SCHEMA_PREFIX': f"{self.flow_name}_",
            # Add any custom settings from flow config
            **self.flow_config.custom_settings
        }
        
    def _setup_directories(self):
        """Create necessary directories for this flow"""
        base_dir = Path(self._context['OUTPUT_DIR'])
        
        # Create subdirectories
        (base_dir / 'requirements').mkdir(parents=True, exist_ok=True)
        (base_dir / 'diagrams').mkdir(parents=True, exist_ok=True)
        (base_dir / 'schemas').mkdir(parents=True, exist_ok=True)
        (base_dir / 'scenarios').mkdir(parents=True, exist_ok=True)
        (base_dir / 'logs').mkdir(parents=True, exist_ok=True)
        
    def get(self, key: str, default: Any = None) -> Any:
        """Get context value by key"""
        return self._context.get(key, default)
        
    def get_all(self) -> Dict[str, Any]:
        """Get all context variables"""
        return self._context.copy()
        
    def set(self, key: str, value: Any):
        """Set a context value"""
        self._context[key] = value
        
    def update(self, updates: Dict[str, Any]):
        """Update multiple context values"""
        self._context.update(updates)
        
    def save_context(self):
        """Save context to file for debugging/auditing"""
        context_file = Path(self._context['OUTPUT_DIR']) / 'flow_context.json'
        with open(context_file, 'w') as f:
            json.dump(self._context, f, indent=2)
            
    def __repr__(self):
        return f"FlowContext(flow_name={self.flow_name})"
```

---

### 4. Enhanced Base Agent with Flow Context

```python
# agents/base_agent.py

from abc import ABC, abstractmethod
from typing import Dict, Any
from shared.template_engine import TemplateEngine
from shared.flow_context import FlowContext
import structlog

logger = structlog.get_logger()

class BaseAgent(ABC):
    """Enhanced base agent with flow context support"""
    
    def __init__(self, config: Dict, flow_context: FlowContext):
        self.config = config
        self.flow_context = flow_context
        self.template_engine = TemplateEngine()
        self.name = self.__class__.__name__
        
    def get_prompt_template(self, template_name: str) -> str:
        """
        Load and render prompt template with flow context
        
        Args:
            template_name: Name of template file (e.g., 'code-analysis.md')
            
        Returns:
            Rendered prompt with flow-specific values
        """
        template_path = f"config/prompts/{template_name}"
        prompt = self.template_engine.render_file(
            template_path,
            self.flow_context.get_all()
        )
        return prompt
        
    def get_output_path(self, filename: str) -> str:
        """Get flow-specific output path"""
        base_dir = self.flow_context.get('OUTPUT_DIR')
        return f"{base_dir}/{filename}"
        
    def log(self, message: str, **kwargs):
        """Log with flow context"""
        logger.info(
            message,
            agent=self.name,
            flow_name=self.flow_context.flow_name,
            **kwargs
        )
        
    @abstractmethod
    def execute(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """Execute agent logic - must be implemented by subclasses"""
        pass
        
    def run(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """Wrapper for execute with logging and error handling"""
        self.log(f"Starting {self.name}")
        
        try:
            result = self.execute(inputs)
            self.log(f"Completed {self.name}", status="success")
            return result
        except Exception as e:
            self.log(f"Failed {self.name}", status="error", error=str(e))
            raise
```

---

### 5. Example: Code Analysis Agent with Flow Context

```python
# agents/code_analysis_agent.py

from agents.base_agent import BaseAgent
from typing import Dict, Any
import subprocess
import json

class CodeAnalysisAgent(BaseAgent):
    """Analyzes codebase for a specific flow"""
    
    def execute(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """
        Analyze code for the current flow
        
        Args:
            inputs: {
                'codebase_path': str,
                'mcp_connections': List[str]
            }
        """
        # Get flow-specific prompt
        prompt = self.get_prompt_template('code-analysis.md')
        
        # Get flow-specific entry point
        entry_point = self.flow_context.get('FLOW_ENTRY_POINT')
        flow_name = self.flow_context.get('FLOW_NAME')
        
        self.log(f"Analyzing flow: {flow_name} starting from {entry_point}")
        
        # Build Claude Code command with flow context
        output_path = self.get_output_path('code-analysis.json')
        
        cmd = [
            'claude-code',
            '--prompt', self._create_flow_specific_prompt(prompt, entry_point),
            '--input', inputs['codebase_path'],
            '--output', output_path,
            '--mcp-config', 'config/mcp-config.json'
        ]
        
        # Execute Claude Code CLI
        result = subprocess.run(cmd, capture_output=True, text=True)
        
        if result.returncode != 0:
            raise Exception(f"Claude Code failed: {result.stderr}")
            
        # Load and return results
        with open(output_path, 'r') as f:
            analysis = json.load(f)
            
        # Add flow context to results
        analysis['flow_name'] = flow_name
        analysis['flow_entry_point'] = entry_point
        
        return analysis
        
    def _create_flow_specific_prompt(self, base_prompt: str, entry_point: str) -> str:
        """Enhance prompt with flow-specific instructions"""
        enhanced_prompt = f"""
{base_prompt}

## Flow-Specific Context

**Flow Name:** {self.flow_context.get('FLOW_NAME')}
**Description:** {self.flow_context.get('FLOW_DESCRIPTION')}
**Entry Point:** {entry_point}

**Special Instructions:**
- Focus analysis on code paths starting from {entry_point}
- Trace dependencies specific to this flow
- Identify flow-specific data models and schemas
- Note any flow-specific configuration or environment variables

**Output Requirements:**
All identifiers should be prefixed with flow name for traceability.
"""
        return enhanced_prompt
```

---

### 6. Flow-Specific Orchestrator

```python
# agents/flow_orchestrator.py

from typing import Dict, Any, List
from agents.base_agent import BaseAgent
from shared.flow_context import FlowContext
from agents.code_analysis_agent import CodeAnalysisAgent
from agents.requirement_extraction_agent import RequirementExtractionAgent
from agents.dependency_analysis_agent import DependencyAnalysisAgent
from agents.sequence_diagram_agent import SequenceDiagramAgent
from agents.scenario_generation_agent import ScenarioGenerationAgent
from agents.schema_extraction_agent import SchemaExtractionAgent
from agents.output_compiler_agent import OutputCompilerAgent
import structlog

logger = structlog.get_logger()

class FlowOrchestrator:
    """Orchestrates all agents for a single flow"""
    
    def __init__(self, config: Dict, flow_context: FlowContext):
        self.config = config
        self.flow_context = flow_context
        self.flow_name = flow_context.flow_name
        
        # Initialize all agents with flow context
        self.agents = {
            'code_analysis': CodeAnalysisAgent(config, flow_context),
            'requirement_extraction': RequirementExtractionAgent(config, flow_context),
            'dependency_analysis': DependencyAnalysisAgent(config, flow_context),
            'sequence_diagram': SequenceDiagramAgent(config, flow_context),
            'scenario_generation': ScenarioGenerationAgent(config, flow_context),
            'schema_extraction': SchemaExtractionAgent(config, flow_context),
            'output_compiler': OutputCompilerAgent(config, flow_context)
        }
        
    def run_full_pipeline(self, codebase_path: str) -> Dict[str, Any]:
        """
        Execute complete requirement gathering pipeline for this flow
        
        Args:
            codebase_path: Path to codebase
            
        Returns:
            Dictionary with all results
        """
        logger.info(
            "Starting flow pipeline",
            flow_name=self.flow_name,
            codebase_path=codebase_path
        )
        
        results = {'flow_name': self.flow_name}
        
        try:
            # Step 1: Code Analysis (Foundation)
            logger.info("Step 1: Code Analysis", flow_name=self.flow_name)
            code_analysis = self.agents['code_analysis'].run({
                'codebase_path': codebase_path,
                'mcp_connections': ['database']
            })
            results['code_analysis'] = code_analysis
            
            # Step 2: Requirement Extraction
            logger.info("Step 2: Requirement Extraction", flow_name=self.flow_name)
            requirements = self.agents['requirement_extraction'].run({
                'code_analysis': code_analysis
            })
            results['requirements'] = requirements
            
            # Step 3: Dependency Analysis
            logger.info("Step 3: Dependency Analysis", flow_name=self.flow_name)
            dependencies = self.agents['dependency_analysis'].run({
                'requirements': requirements,
                'code_analysis': code_analysis
            })
            results['dependencies'] = dependencies
            
            # Step 4: Parallel execution (Diagrams, Scenarios, Schemas)
            logger.info("Step 4: Parallel Artifact Generation", flow_name=self.flow_name)
            
            sequence_diagrams = self.agents['sequence_diagram'].run({
                'requirements': requirements,
                'code_analysis': code_analysis
            })
            results['sequence_diagrams'] = sequence_diagrams
            
            scenarios = self.agents['scenario_generation'].run({
                'requirements': requirements,
                'dependencies': dependencies
            })
            results['scenarios'] = scenarios
            
            schemas = self.agents['schema_extraction'].run({
                'code_analysis': code_analysis,
                'mcp_connections': ['database', 'hive']
            })
            results['schemas'] = schemas
            
            # Step 5: Compile final output
            logger.info("Step 5: Compiling Final Output", flow_name=self.flow_name)
            final_output = self.agents['output_compiler'].run({
                'flow_name': self.flow_name,
                'requirements': requirements,
                'dependencies': dependencies,
                'sequence_diagrams': sequence_diagrams,
                'scenarios': scenarios,
                'schemas': schemas
            })
            results['final_output'] = final_output
            
            # Step 6: JIRA Sync
            logger.info("Step 6: JIRA Sync", flow_name=self.flow_name)
            jira_results = self._sync_to_jira(requirements, dependencies)
            results['jira_sync'] = jira_results
            
            # Save flow context for audit trail
            self.flow_context.save_context()
            
            logger.info(
                "Flow pipeline completed successfully",
                flow_name=self.flow_name
            )
            
            return results
            
        except Exception as e:
            logger.error(
                "Flow pipeline failed",
                flow_name=self.flow_name,
                error=str(e)
            )
            raise
            
    def _sync_to_jira(self, requirements: Dict, dependencies: Dict) -> Dict:
        """Sync requirements to JIRA with flow-specific epic"""
        from shared.mcp_client import JIRAMCPClient
        
        jira_client = JIRAMCPClient(self.config['jira_mcp'])
        
        # Create flow-specific epic
        epic_name = self.flow_context.get('JIRA_EPIC')
        project = self.flow_context.get('JIRA_PROJECT')
        
        epic = jira_client.create_epic({
            'project': project,
            'summary': epic_name,
            'description': f"Requirements for {self.flow_name} flow\n\n{self.flow_context.get('FLOW_DESCRIPTION')}"
        })
        
        # Create stories for each requirement
        ticket_map = {}
        for req in requirements.get('items', []):
            story = jira_client.create_story({
                'project': project,
                'epic': epic['key'],
                'summary': f"[{self.flow_name}] {req['id']}: {req['title']}",
                'description': req['description'],
                'labels': [self.flow_name, req.get('priority', 'medium')]
            })
            ticket_map[req['id']] = story['key']
            
        return {
            'epic_key': epic['key'],
            'stories_created': len(ticket_map),
            'ticket_map': ticket_map
        }
```

---

### 7. Multi-Flow Orchestrator (Run All Flows)

```python
# run_all_flows.py

from utils.config_loader import FlowConfigLoader
from agents.flow_orchestrator import FlowOrchestrator
from shared.flow_context import FlowContext
import yaml
import structlog
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

logger = structlog.get_logger()

class MultiFlowOrchestrator:
    """Orchestrates requirement gathering across multiple flows"""
    
    def __init__(self, config_path: str = 'config/agent-config.yaml'):
        # Load agent configuration
        with open(config_path, 'r') as f:
            self.agent_config = yaml.safe_load(f)
            
        # Load flow configurations
        self.flow_loader = FlowConfigLoader()
        
    def run_all_flows(self, codebase_path: str, parallel: bool = False):
        """
        Run requirement gathering for all enabled flows
        
        Args:
            codebase_path: Path to codebase
            parallel: If True, run flows in parallel
        """
        flows = self.flow_loader.get_sorted_flows()  # Sorted by priority
        
        logger.info(
            "Starting multi-flow requirement gathering",
            total_flows=len(flows),
            parallel=parallel
        )
        
        start_time = datetime.now()
        
        if parallel:
            results = self._run_parallel(flows, codebase_path)
        else:
            results = self._run_sequential(flows, codebase_path)
            
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        
        # Generate summary report
        self._generate_summary_report(results, duration)
        
        logger.info(
            "Multi-flow requirement gathering completed",
            total_flows=len(flows),
            duration_seconds=duration
        )
        
        return results
        
    def _run_sequential(self, flows, codebase_path):
        """Run flows one after another"""
        results = {}
        
        for i, flow in enumerate(flows, 1):
            logger.info(
                f"Processing flow {i}/{len(flows)}",
                flow_name=flow.name
            )
            
            try:
                result = self._process_flow(flow, codebase_path)
                results[flow.name] = {
                    'status': 'success',
                    'result': result
                }
            except Exception as e:
                logger.error(
                    "Flow processing failed",
                    flow_name=flow.name,
                    error=str(e)
                )
                results[flow.name] = {
                    'status': 'error',
                    'error': str(e)
                }
                
        return results
        
    def _run_parallel(self, flows, codebase_path, max_workers: int = 3):
        """Run flows in parallel with thread pool"""
        results = {}
        
        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            # Submit all flows
            future_to_flow = {
                executor.submit(self._process_flow, flow, codebase_path): flow
                for flow in flows
            }
            
            # Collect results as they complete
            for future in as_completed(future_to_flow):
                flow = future_to_flow[future]
                try:
                    result = future.result()
                    results[flow.name] = {
                        'status': 'success',
                        'result': result
                    }
                    logger.info("Flow completed", flow_name=flow.name)
                except Exception as e:
                    logger.error(
                        "Flow processing failed",
                        flow_name=flow.name,
                        error=str(e)
                    )
                    results[flow.name] = {
                        'status': 'error',
                        'error': str(e)
                    }
                    
        return results
        
    def _process_flow(self, flow_config, codebase_path):
        """Process a single flow"""
        # Create flow context
        flow_context = FlowContext(
            flow_config,
            self.flow_loader.global_settings
        )
        
        # Create flow orchestrator
        orchestrator = FlowOrchestrator(
            self.agent_config,
            flow_context
        )
        
        # Run pipeline
        result = orchestrator.run_full_pipeline(codebase_path)
        
        return result
        
    def _generate_summary_report(self, results: Dict, duration: float):
        """Generate summary report across all flows"""
        total_flows = len(results)
        successful = sum(1 for r in results.values() if r['status'] == 'success')
        failed = total_flows - successful
        
        report = f"""
# Multi-Flow Requirement Gathering Summary

**Execution Date:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
**Total Duration:** {duration:.2f} seconds
**Total Flows Processed:** {total_flows}
**Successful:** {successful}
**Failed:** {failed}

## Flow Results

"""
        
        for flow_name, result in results.items():
            status_emoji = "✅" if result['status'] == 'success' else "❌"
            report += f"\n### {status_emoji} {flow_name}\n"
            report += f"**Status:** {result['status']}\n"
            
            if result['status'] == 'success':
                flow_result = result['result']
                report += f"- Requirements Extracted: {len(flow_result.get('requirements', {}).get('items', []))}\n"
                report += f"- JIRA Stories Created: {flow_result.get('jira_sync', {}).get('stories_created', 0)}\n"
                report += f"- JIRA Epic: {flow_result.get('jira_sync', {}).get('epic_key', 'N/A')}\n"
            else:
                report += f"- Error: {result['error']}\n"
                
        # Save report
        with open('outputs/multi_flow_summary.md', 'w') as f:
            f.write(report)
            
        logger.info("Summary report generated", path='outputs/multi_flow_summary.md')

# Main execution
if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Run requirement gathering for all flows')
    parser.add_argument('--codebase', required=True, help='Path to codebase')
    parser.add_argument('--parallel', action='store_true', help='Run flows in parallel')
    parser.add_argument('--config', default='config/agent-config.yaml', help='Agent config file')
    
    args = parser.parse_args()
    
    orchestrator = MultiFlowOrchestrator(args.config)
    results = orchestrator.run_all_flows(args.codebase, parallel=args.parallel)
    
    print(f"\n✅ Processing complete! Check outputs/multi_flow_summary.md for details")
```

---

### 8. Single Flow Execution Script

```python
# run.py

from utils.config_loader import FlowConfigLoader
from agents.flow_orchestrator import FlowOrchestrator
from shared.flow_context import FlowContext
import yaml
import argparse
import structlog

logger = structlog.get_logger()

def main():
    parser = argparse.ArgumentParser(description='Run requirement gathering for a single flow')
    parser.add_argument('--flow-name', required=True, help='Name of the flow to process')
    parser.add_argument('--codebase', required=True, help='Path to codebase')
    parser.add_argument('--config', default='config/agent-config.yaml', help='Agent config file')
    
    args = parser.parse_args()
    
    # Load configurations
    flow_loader = FlowConfigLoader()
    flow_config = flow_loader.get_flow_by_name(args.flow_name)
    
    if not flow_config:
        print(f"❌ Flow '{args.flow_name}' not found in configuration")
        print(f"Available flows: {[f.name for f in flow_loader.get_all_flows()]}")
        return
        
    if not flow_config.enabled:
        print(f"⚠️  Flow '{args.flow_name}' is disabled in configuration")
        return
        
    # Load agent config
    with open(args.config, 'r') as f:
        agent_config = yaml.safe_load(f)
        
    # Create flow context
    flow_context = FlowContext(flow_config, flow_loader.global_settings)
    
    # Create orchestrator
    orchestrator = FlowOrchestrator(agent_config, flow_context)
    
    # Run pipeline
    logger.info("Starting requirement gathering", flow_name=args.flow_name)
    
    try:
        result = orchestrator.run_full_pipeline(args.codebase)
        
        output_dir = flow_context.get('OUTPUT_DIR')
        print(f"\n✅ Success! Results saved to: {output_dir}")
        print(f"📊 Requirements extracted: {len(result.get('requirements', {}).get('items', []))}")
        print(f"📝 JIRA Epic: {result.get('jira_sync', {}).get('epic_key', 'N/A')}")
        
    except Exception as e:
        logger.error("Pipeline failed", flow_name=args.flow_name, error=str(e))
        print(f"\n❌ Error: {str(e)}")
        raise

if __name__ == "__main__":
    main()
```

---

### 9. Batch Flow Execution (Selective)

```python
# run_flow_batch.py

from utils.config_loader import FlowConfigLoader
from run_all_flows import MultiFlowOrchestrator
import argparse

def main():
    parser = argparse.ArgumentParser(description='Run requirement gathering for selected flows')
    parser.add_argument('--flows', nargs='+', required=True, help='Flow names to process')
    parser.add_argument('--codebase', required=True, help='Path to codebase')
    parser.add_argument('--parallel', action='store_true', help='Run flows in parallel')
    parser.add_argument('--priority', choices=['high', 'medium', 'low'], help='Filter by priority')
    
    args = parser.parse_args()
    
    # Load flow configurations
    flow_loader = FlowConfigLoader()
    
    # Get flows to process
    if args.priority:
        flows = flow_loader.get_flows_by_priority(args.priority)
        print(f"Processing all {args.priority} priority flows: {[f.name for f in flows]}")
    else:
        flows = [flow_loader.get_flow_by_name(name) for name in args.flows]
        flows = [f for f in flows if f is not None and f.enabled]
        
        if len(flows) != len(args.flows):
            missing = set(args.flows) - {f.name for f in flows}
            print(f"⚠️  Some flows not found or disabled: {missing}")
            
    if not flows:
        print("❌ No valid flows to process")
        return
        
    # Process flows
    orchestrator = MultiFlowOrchestrator()
    
    print(f"\n🚀 Processing {len(flows)} flows...")
    for flow in flows:
        print(f"  - {flow.name} ({flow.priority})")
        
    # Temporarily update flow loader with selected flows
    original_flows = flow_loader.flows
    flow_loader.flows = flows
    
    results = orchestrator.run_all_flows(args.codebase, parallel=args.parallel)
    
    # Restore original flows
    flow_loader.flows = original_flows
    
    print(f"\n✅ Batch processing complete!")

if __name__ == "__main__":
    main()
```

---

## Enhanced Prompt Templates with {FLOW_NAME}

### config/prompts/code-analysis.md

```markdown
# Code Analysis Prompt - {FLOW_NAME}

You are analyzing the **{FLOW_NAME}** flow in the codebase.

## Flow Context
- **Flow Name:** {FLOW_NAME}
- **Description:** {FLOW_DESCRIPTION}
- **Entry Point:** {FLOW_ENTRY_POINT}
- **Priority:** {FLOW_PRIORITY}

## Analysis Tasks

1. **Trace Flow Execution Path**
   - Start from entry point: {FLOW_ENTRY_POINT}
   - Follow all code paths through this flow
   - Identify all functions/methods called
   - Map out the complete execution flow

2. **Extract Flow Components**
   - Controllers/Handlers specific to this flow
   - Services used by this flow
   - Data models accessed
   - External APIs called
   - Database tables used (prefix: {DB_SCHEMA_PREFIX})

3. **Identify Dependencies**
   - Other flows this depends on
   - Shared libraries/utilities
   - External services
   - Configuration requirements

4. **Data Flow Analysis**
   - Input data structures
   - Data transformations
   - Output data structures
   - Database operations

## Output Format

Return JSON with this structure:

```json
{
  "flow_name": "{FLOW_NAME}",
  "entry_point": "{FLOW_ENTRY_POINT}",
  "execution_path": [...],
  "components": {
    "controllers": [...],
    "services": [...],
    "models": [...],
    "utilities": [...]
  },
  "dependencies": {
    "internal_flows": [...],
    "external_services": [...],
    "libraries": [...]
  },
  "data_flow": {
    "inputs": [...],
    "transformations": [...],
    "outputs": [...],
    "database_operations": [...]
  },
  "configuration": [...]
}
```

Be thorough and accurate. This analysis will be used by all downstream agents.
```

---

### config/prompts/requirement-extraction.md

```markdown
# Requirement Extraction Prompt - {FLOW_NAME}

Extract functional and non-functional requirements for the **{FLOW_NAME}** flow.

## Flow Context
- **Flow Name:** {FLOW_NAME}
- **Description:** {FLOW_DESCRIPTION}

## Input
You will receive code analysis results for this flow.

## Requirements to Extract

1. **Functional Requirements**
   - What does the {FLOW_NAME} flow do?
   - What business logic is implemented?
   - What user actions trigger this flow?
   - What are the expected outcomes?

2. **Non-Functional Requirements**
   - Performance requirements (response time, throughput)
   - Security requirements (authentication, authorization)
   - Reliability requirements
   - Scalability considerations

3. **Business Rules**
   - Validation rules
   - Calculation logic
   - Decision logic
   - Workflow rules

## Output Format

```markdown
## Requirements for {FLOW_NAME}

### REQ-{FLOW_NAME}-001: [Requirement Title]
**Type:** Functional
**Priority:** High
**Description:** Detailed description...

**Acceptance Criteria:**
- [ ] Criterion 1
- [ ] Criterion 2

**Related Components:**
- Component A
- Component B

---

### REQ-{FLOW_NAME}-002: [Requirement Title]
...
```

**Naming Convention:** All requirement IDs must be prefixed with `REQ-{FLOW_NAME}-` for traceability.
```

---

## Usage Examples

### Example 1: Process Single Flow

```bash
# Process the user authentication flow
python run.py \
  --flow-name user_authentication_flow \
  --codebase /path/to/repo

# Output:
# outputs/user_authentication_flow/
#   ├── requirements/
#   ├── diagrams/
#   ├── schemas/
#   ├── scenarios/
#   └── flow_context.json
```

### Example 2: Process All Flows (Sequential)

```bash
# Process all enabled flows one by one
python run_all_flows.py \
  --codebase /path/to/repo

# Output:
# outputs/
#   ├── user_authentication_flow/
#   ├── payment_processing_flow/
#   ├── order_fulfillment_flow/
#   ├── notification_flow/
#   └── multi_flow_summary.md
```

### Example 3: Process All Flows (Parallel)

```bash
# Process all flows in parallel (faster for large codebases)
python run_all_flows.py \
  --codebase /path/to/repo \
  --parallel

# Runs up to 3 flows concurrently
```

### Example 4: Process Selected Flows

```bash
# Process only specific flows
python run_flow_batch.py \
  --flows user_authentication_flow payment_processing_flow \
  --codebase /path/to/repo \
  --parallel
```

### Example 5: Process by Priority

```bash
# Process all high-priority flows first
python run_flow_batch.py \
  --priority high \
  --codebase /path/to/repo
```

---

## Directory Structure After Execution

```
outputs/
├── user_authentication_flow/
│   ├── requirements/
│   │   └── requirements.md
│   ├── diagrams/
│   │   ├── sequence_login.mmd
│   │   ├── sequence_logout.mmd
│   │   └── flow_diagram.mmd
│   ├── schemas/
│   │   ├── LoginRequest.yaml
│   │   ├── LoginResponse.yaml
│   │   └── database_schema.yaml
│   ├── scenarios/
│   │   └── scenarios.md
│   ├── logs/
│   │   └── execution.log
│   ├── flow_context.json
│   └── final_report.md
│
├── payment_processing_flow/
│   ├── requirements/
│   ├── diagrams/
│   ├── schemas/
│   ├── scenarios/
│   ├── logs/
│   ├── flow_context.json
│   └── final_report.md
│
└── multi_flow_summary.md
```

---

## JIRA Output Structure

When synced to JIRA, the structure will be:

```
Project: PROJ

Epic: Requirements - user_authentication_flow
  └─ Story: [user_authentication_flow] REQ-user_authentication_flow-001: User Login
  └─ Story: [user_authentication_flow] REQ-user_authentication_flow-002: Session Management
  └─ Story: [user_authentication_flow] REQ-user_authentication_flow-003: Password Reset

Epic: Requirements - payment_processing_flow
  └─ Story: [payment_processing_flow] REQ-payment_processing_flow-001: Payment Gateway Integration
  └─ Story: [payment_processing_flow] REQ-payment_processing_flow-002: Transaction Validation
  └─ Story: [payment_processing_flow] REQ-payment_processing_flow-003: Refund Processing
```

---

## Advanced Configuration Options

### Per-Flow Custom Settings

```yaml
# flows-config.yaml

flows:
  - name: payment_processing_flow
    description: "Payment gateway integration"
    entry_point: "src/payments/processor.py"
    enabled: true
    priority: high
    custom_settings:
      # Flow-specific MCP connections
      mcp_connections:
        - database
        - payment_gateway_mcp
      
      # Flow-specific output requirements
      output_formats:
        - markdown
        - json
        - openapi
        
      # Flow-specific JIRA settings
      jira_labels:
        - payments
        - critical
        - pci-compliant
        
      # Flow-specific validation rules
      validation:
        min_requirements: 5
        require_security_review: true
        
      # Flow-specific agent overrides
      agent_overrides:
        code_analysis:
          timeout: 900  # 15 minutes for complex payment flow
        requirement_extraction:
          include_compliance_checks: true
```

### Dynamic Context Variables

You can add ANY custom variables to flow context:

```python
# In flow_context.py

def _build_context(self) -> Dict[str, Any]:
    """Build the complete context dictionary"""
    context = {
        'FLOW_NAME': self.flow_name,
        'FLOW_DESCRIPTION': self.flow_config.description,
        # ... standard variables ...
        
        # Add custom variables from config
        **self.flow_config.custom_settings
    }
    
    # Add computed variables
    context['FLOW_NAME_UPPER'] = self.flow_name.upper()
    context['FLOW_NAME_CAMEL'] = self._to_camel_case(self.flow_name)
    context['TIMESTAMP'] = datetime.now().isoformat()
    context['OUTPUT_JSON'] = f"{context['OUTPUT_DIR']}/output.json"
    context['OUTPUT_MD'] = f"{context['OUTPUT_DIR']}/report.md"
    
    return context
```

Then use in prompts:

```markdown
# Output paths
- JSON: {OUTPUT_JSON}
- Markdown: {OUTPUT_MD}
- Timestamp: {TIMESTAMP}
```

---

## Monitoring & Debugging

### View Flow Execution Logs

```bash
# View logs for specific flow
tail -f outputs/user_authentication_flow/logs/execution.log

# View all logs
tail -f outputs/*/logs/execution.log
```

### Inspect Flow Context

```bash
# Check what context was used for a flow
cat outputs/user_authentication_flow/flow_context.json
```

### Debug Failed Flows

```python
# Add to run_all_flows.py for debugging

def _process_flow(self, flow_config, codebase_path):
    """Process a single flow with enhanced error reporting"""
    try:
        # ... existing code ...
    except Exception as e:
        # Save debug information
        debug_info = {
            'flow_name': flow_config.name,
            'error': str(e),
            'traceback': traceback.format_exc(),
            'flow_config': flow_config.__dict__,
            'timestamp': datetime.now().isoformat()
        }
        
        debug_path = f"outputs/{flow_config.name}/debug_error.json"
        with open(debug_path, 'w') as f:
            json.dump(debug_info, f, indent=2)
            
        raise
```

---

## Best Practices

1. **Flow Naming Convention**
   - Use snake_case: `user_authentication_flow`
   - Be descriptive: `payment_refund_processing_flow` not `refund_flow`
   - Consistent suffix: Always end with `_flow`

2. **Requirement ID Convention**
   - Format: `REQ-{FLOW_NAME}-{NUMBER}`
   - Example: `REQ-user_authentication_flow-001`
   - Ensures global uniqueness across all flows

3. **Priority Management**
   - Run high-priority flows first (sequential mode)
   - Or run all in parallel if resources allow

4. **Error Handling**
   - Failed flows don't block other flows
   - Each flow has independent error logs
   - Summary report shows overall status

5. **Caching Strategy**
   - Cache code analysis results per flow
   - Reuse if code hasn't changed
   - Cache key: `{flow_name}_{codebase_hash}`

---

## Conclusion

This dynamic flow configuration system allows you to:

✅ **Process multiple flows** from a single configuration file
✅ **Dynamically replace {FLOW_NAME}** and other placeholders everywhere
✅ **Run flows sequentially or in parallel**
✅ **Filter by priority or specific flow names**
✅ **Track each flow independently** with its own output directory
✅ **Sync to JIRA** with flow-specific epics and stories
✅ **Scale easily** to hundreds of flows

Start with a few flows, validate the outputs, then scale to your entire codebase!
