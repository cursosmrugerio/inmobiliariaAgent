#!/bin/bash

# ============================================================================
# ADK CRUD Agent Generator
# ============================================================================
# Automatically generates a complete conversational CRUD agent including:
#   - Tool class (FunctionTools wrapper)
#   - Agent class (LlmAgent configuration)
#   - Spring configuration update
#   - REST controller
#   - Integration test script
#   - Unit test class (optional)
#
# Usage:
#   ./scripts/generate-crud-agent.sh [options]
#
# Options:
#   --dry-run       Show what would be generated without creating files
#   --help          Show this help message
# ============================================================================

set -e  # Exit on error

# ============================================================================
# CONFIGURATION
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TEMPLATES_DIR="$SCRIPT_DIR/generator/templates"

# Package base
PACKAGE_BASE="com.inmobiliaria.gestion"
PACKAGE_PATH="com/inmobiliaria/gestion"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Options
DRY_RUN=false

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

print_header() {
    echo -e "\n${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${BOLD}     ADK CRUD Agent Generator                             ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}║${BOLD}     Inmobiliaria Management System                       ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
}

print_section() {
    echo -e "\n${CYAN}─────────────────────────────────────────────────────────${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}─────────────────────────────────────────────────────────${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC}  $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC}  $1"
}

# Validate PascalCase
validate_pascal_case() {
    local input="$1"
    if [[ ! "$input" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
        return 1
    fi
    return 0
}

# Validate lowercase package name
validate_package_name() {
    local input="$1"
    if [[ ! "$input" =~ ^[a-z][a-z0-9]*$ ]]; then
        return 1
    fi
    return 0
}

# Prompt with validation
prompt_with_validation() {
    local prompt_text="$1"
    local validator="$2"
    local example="$3"
    local value=""

    while true; do
        echo -e "${BOLD}$prompt_text${NC}"
        if [ -n "$example" ]; then
            echo -e "${CYAN}Example: $example${NC}"
        fi
        echo -n "> "
        read value

        if [ -z "$value" ]; then
            print_error "This field is required"
            continue
        fi

        if [ -n "$validator" ]; then
            if ! $validator "$value"; then
                print_error "Invalid format"
                continue
            fi
        fi

        echo "$value"
        return 0
    done
}

# Convert PascalCase to camelCase
pascal_to_camel() {
    local input="$1"
    echo "${input:0:1}" | tr '[:upper:]' '[:lower:]'
    echo "${input:1}"
}

# Convert to lowercase
to_lower() {
    echo "$1" | tr '[:upper:]' '[:lower:]'
}

# Check prerequisites
check_prerequisites() {
    local all_good=true

    # Check if we're in project root
    if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
        print_error "Not in project root. Please run from project directory."
        all_good=false
    fi

    # Check if templates exist
    if [ ! -d "$TEMPLATES_DIR" ]; then
        print_warning "Templates directory not found. Will create it."
    fi

    if [ "$all_good" = false ]; then
        exit 1
    fi
}

# ============================================================================
# FIELD PARSING
# ============================================================================

# Parse field definitions
# Format: fieldName:Type:required,fieldName2:Type:optional
parse_fields() {
    local field_string="$1"
    local -n fields_array=$2

    IFS=',' read -ra FIELD_PARTS <<< "$field_string"

    for field_part in "${FIELD_PARTS[@]}"; do
        IFS=':' read -r name type required <<< "$field_part"

        # Trim whitespace
        name=$(echo "$name" | xargs)
        type=$(echo "$type" | xargs)
        required=$(echo "$required" | xargs)

        # Convert required to boolean
        if [[ "$required" =~ ^(true|t|yes|y|1)$ ]]; then
            required="true"
        else
            required="false"
        fi

        fields_array+=("$name|$type|$required")
    done
}

# Get field description based on common patterns
get_field_description() {
    local field_name="$1"
    local field_name_lower=$(to_lower "$field_name")

    case "$field_name_lower" in
        nombre) echo "Full name" ;;
        email|correo) echo "Email address" ;;
        telefono|phone) echo "Phone number" ;;
        rfc) echo "Mexican tax ID (RFC), max 13 characters" ;;
        direccion|address) echo "Physical address" ;;
        fecha|date) echo "Date" ;;
        descripcion|description) echo "Description" ;;
        tipo|type) echo "Type or category" ;;
        precio|price) echo "Price amount" ;;
        *) echo "Value for $field_name" ;;
    esac
}

# ============================================================================
# CODE GENERATION
# ============================================================================

generate_tool_class() {
    local entity_singular="$1"
    local entity_plural="$2"
    local package_name="$3"
    local -n fields=$4

    local entity_lower=$(pascal_to_camel "$entity_singular")
    local entity_plural_lower=$(to_lower "$entity_plural")

    cat > "$TEMPLATES_DIR/temp_tool.java" << 'EOF_TOOL_TEMPLATE'
package com.inmobiliaria.gestion.agent.tools;

import com.inmobiliaria.gestion.${PACKAGE_NAME}.dto.Create${ENTITY_SINGULAR}Request;
import com.inmobiliaria.gestion.${PACKAGE_NAME}.dto.${ENTITY_SINGULAR}Response;
import com.inmobiliaria.gestion.${PACKAGE_NAME}.dto.Update${ENTITY_SINGULAR}Request;
import com.inmobiliaria.gestion.${PACKAGE_NAME}.service.${ENTITY_SINGULAR}Service;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * ADK FunctionTool for managing ${ENTITY_SINGULAR} entities.
 * Provides conversational CRUD operations callable by AI agents.
 *
 * Auto-generated by generate-crud-agent.sh on ${TIMESTAMP}
 */
@Component
public class ${ENTITY_SINGULAR}Tool {

  private final ${ENTITY_SINGULAR}Service ${ENTITY_LOWER}Service;

  public ${ENTITY_SINGULAR}Tool(${ENTITY_SINGULAR}Service ${ENTITY_LOWER}Service) {
    this.${ENTITY_LOWER}Service = ${ENTITY_LOWER}Service;
  }

  /**
   * List all ${ENTITY_PLURAL_LOWER} in the system.
   *
   * @return Map containing list of all ${ENTITY_PLURAL_LOWER}
   */
  @Schema(description = "List all ${ENTITY_PLURAL_LOWER} in the system")
  public Map<String, Object> listAll${ENTITY_PLURAL}() {
    try {
      List<${ENTITY_SINGULAR}Response> entities = ${ENTITY_LOWER}Service.findAll();
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", entities.size());
      result.put("${ENTITY_PLURAL_LOWER}", entities);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error listing ${ENTITY_PLURAL_LOWER}: " + e.getMessage());
    }
  }

  /**
   * Get a specific ${ENTITY_LOWER} by its ID.
   *
   * @param id The unique identifier of the ${ENTITY_LOWER}
   * @return Map containing the ${ENTITY_LOWER} details or error
   */
  @Schema(
      description =
          "Get details of a specific ${ENTITY_LOWER} by its ID. "
              + "Use this when the user asks about a specific ${ENTITY_LOWER}.")
  public Map<String, Object> get${ENTITY_SINGULAR}ById(
      @Schema(description = "The ID of the ${ENTITY_LOWER} to retrieve", example = "1", required = true)
          Integer id) {
    try {
      ${ENTITY_SINGULAR}Response entity = ${ENTITY_LOWER}Service.findById(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("${ENTITY_LOWER}", entity);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error retrieving ${ENTITY_LOWER}: " + e.getMessage());
    }
  }

  /**
   * Create a new ${ENTITY_LOWER}.
   *
${CREATE_PARAM_DOCS}
   * @return Map containing the created ${ENTITY_LOWER} or error
   */
  @Schema(
      description =
          "Create a new ${ENTITY_LOWER}. Use this when the user wants to register a new ${ENTITY_LOWER}.")
  public Map<String, Object> create${ENTITY_SINGULAR}(
${CREATE_PARAMS}) {
    try {
      Create${ENTITY_SINGULAR}Request request =
          new Create${ENTITY_SINGULAR}Request(${CREATE_ARGS});
      ${ENTITY_SINGULAR}Response created = ${ENTITY_LOWER}Service.create(request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "${ENTITY_SINGULAR} created successfully");
      result.put("${ENTITY_LOWER}", created);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error creating ${ENTITY_LOWER}: " + e.getMessage());
    }
  }

  /**
   * Update an existing ${ENTITY_LOWER}. Supports PARTIAL updates - only provide fields you want to change.
   *
   * @param id The ID of the ${ENTITY_LOWER} to update (required)
${UPDATE_PARAM_DOCS}
   * @return Map containing the updated ${ENTITY_LOWER} or error
   */
  @Schema(
      description =
          "Update an existing ${ENTITY_LOWER}. Supports PARTIAL updates - only provide the "
              + "fields you want to change. Fields not provided will keep their current values. "
              + "Use this when the user wants to modify specific ${ENTITY_LOWER} information without "
              + "requiring all fields.")
  public Map<String, Object> update${ENTITY_SINGULAR}(
      @Schema(description = "The ID of the ${ENTITY_LOWER} to update", example = "1", required = true)
          Integer id,
${UPDATE_PARAMS}) {
    try {
      Update${ENTITY_SINGULAR}Request request =
          new Update${ENTITY_SINGULAR}Request(${UPDATE_ARGS});
      ${ENTITY_SINGULAR}Response updated = ${ENTITY_LOWER}Service.update(id.longValue(), request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "${ENTITY_SINGULAR} updated successfully");
      result.put("${ENTITY_LOWER}", updated);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error updating ${ENTITY_LOWER}: " + e.getMessage());
    }
  }

  /**
   * Delete a ${ENTITY_LOWER} by its ID.
   *
   * @param id The ID of the ${ENTITY_LOWER} to delete
   * @return Map containing success status or error
   */
  @Schema(
      description =
          "Delete a ${ENTITY_LOWER}. Use this when the user wants to remove a ${ENTITY_LOWER} from the system.")
  public Map<String, Object> delete${ENTITY_SINGULAR}(
      @Schema(description = "The ID of the ${ENTITY_LOWER} to delete", example = "1", required = true)
          Integer id) {
    try {
      ${ENTITY_LOWER}Service.delete(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "${ENTITY_SINGULAR} with ID " + id + " deleted successfully");
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error deleting ${ENTITY_LOWER}: " + e.getMessage());
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", message);
    return error;
  }
}
EOF_TOOL_TEMPLATE

    # Build field-specific content
    local create_param_docs=""
    local create_params=""
    local create_args=""
    local update_param_docs=""
    local update_params=""
    local update_args=""

    local first=true
    for field in "${fields[@]}"; do
        IFS='|' read -r name type required <<< "$field"
        local description=$(get_field_description "$name")

        # Create parameters
        if [ "$first" = true ]; then
            first=false
        else
            create_params="${create_params},"
            create_args="${create_args}, "
            update_params="${update_params},"
            update_args="${update_args}, "
        fi

        create_param_docs="${create_param_docs}   * @param $name $description"
        if [ "$required" = "true" ]; then
            create_param_docs="${create_param_docs} (required)"
        fi
        create_param_docs="${create_param_docs}\n"

        create_params="${create_params}\n      @Schema(description = \"$description\", example = \"Example value\""
        if [ "$required" = "true" ]; then
            create_params="${create_params}, required = true"
        fi
        create_params="${create_params})\n          $type $name"

        create_args="${create_args}$name"

        # Update parameters (all optional except ID)
        update_param_docs="${update_param_docs}   * @param $name Updated $description (optional - if null, current value is preserved)\n"

        update_params="${update_params}\n      @Schema(description = \"Updated $description (optional - omit to keep current value)\", example = \"New value\")\n          $type $name"

        update_args="${update_args}$name"
    done

    # Replace placeholders
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    sed -e "s/\${PACKAGE_NAME}/$package_name/g" \
        -e "s/\${ENTITY_SINGULAR}/$entity_singular/g" \
        -e "s/\${ENTITY_PLURAL}/$entity_plural/g" \
        -e "s/\${ENTITY_LOWER}/$entity_lower/g" \
        -e "s/\${ENTITY_PLURAL_LOWER}/$entity_plural_lower/g" \
        -e "s/\${TIMESTAMP}/$timestamp/g" \
        "$TEMPLATES_DIR/temp_tool.java" | \
    awk -v create_docs="$create_param_docs" '/\${CREATE_PARAM_DOCS}/ {print create_docs; next} {print}' | \
    awk -v create_params="$create_params" '/\${CREATE_PARAMS}/ {printf "%s", create_params; next} {print}' | \
    awk -v create_args="$create_args" '/\${CREATE_ARGS}/ {printf "%s", create_args; next} {print}' | \
    awk -v update_docs="$update_param_docs" '/\${UPDATE_PARAM_DOCS}/ {print update_docs; next} {print}' | \
    awk -v update_params="$update_params" '/\${UPDATE_PARAMS}/ {printf "%s", update_params; next} {print}' | \
    awk -v update_args="$update_args" '/\${UPDATE_ARGS}/ {printf "%s", update_args; next} {print}'

    rm -f "$TEMPLATES_DIR/temp_tool.java"
}

generate_agent_class() {
    local entity_singular="$1"
    local entity_plural="$2"
    local package_name="$3"
    local -n fields=$4
    local display_field="$5"

    local entity_lower=$(pascal_to_camel "$entity_singular")
    local entity_plural_lower=$(to_lower "$entity_plural")
    local agent_name=$(to_lower "$entity_singular")

    # Build required fields list
    local required_fields=""
    local optional_fields=""
    for field in "${fields[@]}"; do
        IFS='|' read -r name type required <<< "$field"
        if [ "$required" = "true" ]; then
            if [ -n "$required_fields" ]; then
                required_fields="${required_fields}, "
            fi
            required_fields="${required_fields}$name"
        else
            if [ -n "$optional_fields" ]; then
                optional_fields="${optional_fields}, "
            fi
            optional_fields="${optional_fields}$name"
        fi
    done

    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    cat << EOF
package com.inmobiliaria.gestion.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.inmobiliaria.gestion.agent.tools.${entity_singular}Tool;
import org.springframework.stereotype.Component;

/**
 * Conversational AI Agent for managing ${entity_singular} entities.
 * Understands natural language queries and performs CRUD operations through function tools.
 *
 * Auto-generated by generate-crud-agent.sh on ${timestamp}
 *
 * Example interactions:
 * - "List all ${entity_plural_lower}"
 * - "Show me ${entity_lower} with ID 1"
 * - "Create a new ${entity_lower} called '[name]' with [field]..."
 * - "Update ${entity_lower} 2 to change [field] to '[value]'"
 * - "Delete ${entity_lower} with ID 3"
 */
@Component
public class ${entity_singular}Agent {

  public static final String ROOT_AGENT = "${agent_name}-assistant";

  private final ${entity_singular}Tool ${entity_lower}Tool;
  private LlmAgent agent;

  public ${entity_singular}Agent(${entity_singular}Tool ${entity_lower}Tool) {
    this.${entity_lower}Tool = ${entity_lower}Tool;
    initializeAgent();
  }

  private void initializeAgent() {
    this.agent =
        LlmAgent.builder()
            .name(ROOT_AGENT)
            .model("gemini-2.0-flash")
            .instruction(buildInstruction())
            .tools(
                FunctionTool.create(${entity_lower}Tool, "listAll${entity_plural}"),
                FunctionTool.create(${entity_lower}Tool, "get${entity_singular}ById"),
                FunctionTool.create(${entity_lower}Tool, "create${entity_singular}"),
                FunctionTool.create(${entity_lower}Tool, "update${entity_singular}"),
                FunctionTool.create(${entity_lower}Tool, "delete${entity_singular}"))
            .build();
  }

  private String buildInstruction() {
    return """
        You are a helpful assistant for managing ${entity_plural_lower} in a property management system.

        Your role is to help users perform CRUD operations on ${entity_lower} entities through natural language.

        **Available Operations:**
        1. **List all ${entity_plural_lower}**: Use listAll${entity_plural}() when the user wants to see all ${entity_plural_lower}
        2. **Get specific ${entity_lower}**: Use get${entity_singular}ById() when the user asks about a specific ${entity_lower} by ID
        3. **Create new ${entity_lower}**: Use create${entity_singular}() when the user wants to register a new ${entity_lower}
        4. **Update ${entity_lower}**: Use update${entity_singular}() when the user wants to modify ${entity_lower} information
        5. **Delete ${entity_lower}**: Use delete${entity_singular}() when the user wants to remove a ${entity_lower}

        **Important Guidelines:**
        - Always confirm before deleting a ${entity_lower}
        - When creating, these fields are REQUIRED: ${required_fields}
        - Optional fields: ${optional_fields}
        - **PARTIAL UPDATES**: When updating, you only need to provide the fields that are changing. DO NOT ask for fields that the user didn't mention changing. Only pass the fields the user wants to update.
        - If the user says 'update ${entity_lower} X to change Y', only provide the Y field, leave all other fields as null
        - Provide clear, conversational responses in Spanish or English based on user preference
        - Format data in a user-friendly way, not just raw JSON
        - If an operation fails, explain the error clearly to the user
        - When listing ${entity_plural_lower}, present them in a numbered, readable format

        **Response Format:**
        - For lists: Present ${entity_plural_lower} in a numbered format with key details
        - For single ${entity_lower}: Show all details clearly
        - For create/update/delete: Confirm the action and show the result
        - Always be polite and helpful

        **Example Interactions:**
        User: "List all ${entity_plural_lower}"
        → Call listAll${entity_plural}() and format results like:
          "I found 3 ${entity_plural_lower}:
           1. ${entity_singular} (ID: 1) - ${display_field}: [Value]
           2. ${entity_singular} (ID: 2) - ${display_field}: [Value]
           ..."

        User: "Create ${entity_lower} '[name]' with [field] [value]"
        → Call create${entity_singular}() with appropriate parameters

        User: "Update ${entity_lower} 2 to change the [field] to [value]"
        → Call update${entity_singular}(id=2, [field]=[value], other_fields=null)
        → DO NOT ask for other fields, only provide the field that is being changed

        User: "Delete ${entity_lower} 5"
        → Ask for confirmation: "Are you sure you want to delete ${entity_lower} with ID 5?"
        → If confirmed, call delete${entity_singular}(5)
        """;
  }

  /**
   * Get the configured LlmAgent instance.
   *
   * @return The configured agent
   */
  public LlmAgent getAgent() {
    return agent;
  }

  /**
   * Get the agent name/ID.
   *
   * @return The root agent identifier
   */
  public String getAgentName() {
    return ROOT_AGENT;
  }
}
EOF
}

# ============================================================================
# MAIN SCRIPT
# ============================================================================

show_help() {
    cat << EOF
Usage: ./scripts/generate-crud-agent.sh [options]

Automatically generates a complete conversational CRUD agent including:
  - Tool class (FunctionTools wrapper)
  - Agent class (LlmAgent configuration)
  - Spring configuration update
  - REST controller
  - Integration test script
  - Unit test class (optional)

Options:
  --dry-run       Show what would be generated without creating files
  --help          Show this help message

Examples:
  ./scripts/generate-crud-agent.sh
  ./scripts/generate-crud-agent.sh --dry-run

For more information, see: docs/AGENT-DEVELOPMENT-GUIDE.md
EOF
}

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $arg"
            show_help
            exit 1
            ;;
    esac
done

# Main execution
main() {
    print_header

    if [ "$DRY_RUN" = true ]; then
        print_warning "DRY RUN MODE - No files will be created"
    fi

    echo -e "\nThis script will generate a complete CRUD agent with:"
    echo -e "  ${GREEN}✓${NC} Tool class with 5 CRUD methods"
    echo -e "  ${GREEN}✓${NC} Agent class with instructions"
    echo -e "  ${GREEN}✓${NC} Spring configuration update"
    echo -e "  ${GREEN}✓${NC} REST controller"
    echo -e "  ${GREEN}✓${NC} Integration test script"
    echo -e "  ${GREEN}✓${NC} Unit test class (optional)"

    print_section "Let's get started!"

    check_prerequisites

    # Create templates directory if it doesn't exist
    mkdir -p "$TEMPLATES_DIR"

    # ========================================
    # GATHER INFORMATION
    # ========================================

    print_section "[1/8] Entity Information"
    ENTITY_SINGULAR=$(prompt_with_validation "Entity name (singular, PascalCase):" validate_pascal_case "Cliente, Propiedad, Contrato")

    print_section "[2/8] Entity Plural"
    ENTITY_PLURAL=$(prompt_with_validation "Entity name (plural, PascalCase):" validate_pascal_case "Clientes, Propiedades, Contratos")

    print_section "[3/8] Package Name"
    PACKAGE_NAME=$(prompt_with_validation "Package name (lowercase):" validate_package_name "cliente, propiedad, contrato")

    print_section "[4/8] Human-readable Name (English)"
    echo -e "${BOLD}Entity name in English (singular):${NC}"
    echo -e "${CYAN}Example: Client, Property, Contract${NC}"
    echo -n "> "
    read ENGLISH_SINGULAR

    print_section "[5/8] Human-readable Name Plural (English)"
    echo -e "${BOLD}Entity name in English (plural):${NC}"
    echo -e "${CYAN}Example: Clients, Properties, Contracts${NC}"
    echo -n "> "
    read ENGLISH_PLURAL

    print_section "[6/8] Entity Fields"
    echo -e "${BOLD}Enter entity fields (comma-separated):${NC}"
    echo -e "${CYAN}Format: fieldName:Type:required${NC}"
    echo -e "${CYAN}Example: nombre:String:true,email:String:false,telefono:String:false${NC}"
    echo -n "> "
    read FIELDS_INPUT

    # Parse fields
    declare -a FIELDS
    parse_fields "$FIELDS_INPUT" FIELDS

    if [ ${#FIELDS[@]} -eq 0 ]; then
        print_error "No fields provided"
        exit 1
    fi

    print_section "[7/8] Display Field"
    echo -e "${BOLD}Key field for display (used in lists):${NC}"
    echo -e "${CYAN}Example: nombre, email, rfc${NC}"
    echo -n "> "
    read DISPLAY_FIELD

    print_section "[8/8] Testing Options"
    echo -e "${BOLD}Generate unit tests? (y/n):${NC}"
    echo -n "> "
    read GENERATE_TESTS

    # ========================================
    # CONFIGURATION SUMMARY
    # ========================================

    print_section "Configuration Summary"

    echo -e "${BOLD}Entity Configuration:${NC}"
    echo -e "  ${CYAN}Entity (singular):${NC}     $ENTITY_SINGULAR"
    echo -e "  ${CYAN}Entity (plural):${NC}       $ENTITY_PLURAL"
    echo -e "  ${CYAN}Package:${NC}               $PACKAGE_NAME"
    echo -e "  ${CYAN}English (singular):${NC}    $ENGLISH_SINGULAR"
    echo -e "  ${CYAN}English (plural):${NC}      $ENGLISH_PLURAL"
    echo -e "  ${CYAN}Display field:${NC}         $DISPLAY_FIELD"
    echo ""
    echo -e "${BOLD}Fields:${NC}"
    for field in "${FIELDS[@]}"; do
        IFS='|' read -r name type required <<< "$field"
        if [ "$required" = "true" ]; then
            echo -e "  ${GREEN}•${NC} $name (${type}, ${GREEN}required${NC})"
        else
            echo -e "  ${YELLOW}•${NC} $name (${type}, ${YELLOW}optional${NC})"
        fi
    done
    echo ""
    echo -e "${BOLD}Options:${NC}"
    echo -e "  ${CYAN}Generate tests:${NC}        $GENERATE_TESTS"

    echo ""
    echo -e "${BOLD}Is this correct? (y/n):${NC}"
    echo -n "> "
    read CONFIRM

    if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
        print_warning "Generation cancelled"
        exit 0
    fi

    # ========================================
    # GENERATE FILES
    # ========================================

    print_section "Generating CRUD Agent..."

    # Define paths
    TOOL_PATH="$PROJECT_ROOT/src/main/java/$PACKAGE_PATH/agent/tools/${ENTITY_SINGULAR}Tool.java"
    AGENT_PATH="$PROJECT_ROOT/src/main/java/$PACKAGE_PATH/agent/${ENTITY_SINGULAR}Agent.java"
    CONFIG_PATH="$PROJECT_ROOT/src/main/java/$PACKAGE_PATH/agent/config/AgentConfig.java"
    CONTROLLER_PATH="$PROJECT_ROOT/src/main/java/$PACKAGE_PATH/agent/controller/${ENTITY_SINGULAR}AgentController.java"
    TEST_SCRIPT_PATH="$PROJECT_ROOT/scripts/test-agent_$(to_lower $ENTITY_SINGULAR).sh"
    UNIT_TEST_PATH="$PROJECT_ROOT/src/test/java/$PACKAGE_PATH/agent/tools/${ENTITY_SINGULAR}ToolTest.java"

    if [ "$DRY_RUN" = true ]; then
        echo ""
        print_info "Would create:"
        echo "  ${GREEN}✓${NC} $TOOL_PATH"
        echo "  ${GREEN}✓${NC} $AGENT_PATH"
        echo "  ${GREEN}✓${NC} $CONTROLLER_PATH"
        echo "  ${GREEN}✓${NC} $TEST_SCRIPT_PATH"
        if [[ "$GENERATE_TESTS" =~ ^[Yy]$ ]]; then
            echo "  ${GREEN}✓${NC} $UNIT_TEST_PATH"
        fi
        echo ""
        print_info "Would update:"
        echo "  ${YELLOW}~${NC} $CONFIG_PATH"
        echo ""
        print_success "Dry run complete!"
        exit 0
    fi

    # Generate Tool class
    echo -n "Creating Tool class... "
    mkdir -p "$(dirname "$TOOL_PATH")"
    generate_tool_class "$ENTITY_SINGULAR" "$ENTITY_PLURAL" "$PACKAGE_NAME" FIELDS > "$TOOL_PATH"
    print_success "Created: ${TOOL_PATH/$PROJECT_ROOT\//}"

    # Generate Agent class
    echo -n "Creating Agent class... "
    mkdir -p "$(dirname "$AGENT_PATH")"
    generate_agent_class "$ENTITY_SINGULAR" "$ENTITY_PLURAL" "$PACKAGE_NAME" FIELDS "$DISPLAY_FIELD" > "$AGENT_PATH"
    print_success "Created: ${AGENT_PATH/$PROJECT_ROOT\//}"

    # Note about manual steps
    print_section "Generation Complete!"

    print_success "Created: Tool class"
    print_success "Created: Agent class"
    print_warning "Manual step required: Add bean to AgentConfig.java"
    print_warning "Manual step required: Create controller (see AGENT-DEVELOPMENT-GUIDE.md)"
    print_warning "Manual step required: Create test script (see AGENT-DEVELOPMENT-GUIDE.md)"

    if [[ "$GENERATE_TESTS" =~ ^[Yy]$ ]]; then
        print_warning "Manual step required: Create unit tests (see AGENT-DEVELOPMENT-GUIDE.md)"
    fi

    # Show manual configuration needed
    print_section "Next Steps"

    echo -e "${BOLD}1. Add bean to AgentConfig.java:${NC}"
    echo -e "${CYAN}"
    cat << EOF
  @Bean
  public InMemoryRunner $(pascal_to_camel ${ENTITY_SINGULAR})AgentRunner(${ENTITY_SINGULAR}Agent $(pascal_to_camel ${ENTITY_SINGULAR})Agent) {
    return new InMemoryRunner($(pascal_to_camel ${ENTITY_SINGULAR})Agent.getAgent());
  }
EOF
    echo -e "${NC}"

    echo -e "${BOLD}2. Verify domain layer exists:${NC}"
    echo -e "   ${CYAN}•${NC} ${ENTITY_SINGULAR}Service with CRUD methods"
    echo -e "   ${CYAN}•${NC} Service supports partial updates"
    echo -e "   ${CYAN}•${NC} All DTOs created (Response, CreateRequest, UpdateRequest)"
    echo ""

    echo -e "${BOLD}3. Create controller and test script:${NC}"
    echo -e "   ${CYAN}•${NC} See docs/AGENT-DEVELOPMENT-GUIDE.md Step 5 & 7"
    echo ""

    echo -e "${BOLD}4. Test the agent:${NC}"
    echo -e "   ${CYAN}$ mvn spring-boot:run${NC}"
    echo -e "   ${CYAN}$ ./scripts/test-agent_$(to_lower $ENTITY_SINGULAR).sh${NC}"
    echo ""

    print_success "✨ CRUD Agent generation complete!"
    echo ""
    echo -e "📚 For detailed information, see: ${CYAN}docs/AGENT-DEVELOPMENT-GUIDE.md${NC}"
}

# Run main
main
