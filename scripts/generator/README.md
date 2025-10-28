# CRUD Agent Generator

**Automated boilerplate code generation for ADK conversational agents**

---

## Overview

The CRUD Agent Generator is a Bash script that automates the creation of complete conversational AI agents for your business entities. Instead of manually writing hundreds of lines of boilerplate code, answer a few questions and generate:

- ✅ **Tool class** - FunctionTools wrapper with all 5 CRUD methods
- ✅ **Agent class** - LlmAgent configuration with instructions
- ✅ **Spring configuration** - Bean setup (manual step reminder)
- ✅ **Controller** - REST endpoint (manual step reminder)
- ✅ **Test script** - Integration testing (manual step reminder)
- ✅ **Unit tests** - Mockito-based tests (optional, manual step reminder)

---

## Quick Start

### Basic Usage

```bash
# Run the generator
./scripts/generate-crud-agent.sh

# Follow the interactive prompts
# The script will guide you through 8 questions
```

### Dry Run Mode

```bash
# See what would be generated without creating files
./scripts/generate-crud-agent.sh --dry-run
```

### Help

```bash
# Show help and usage information
./scripts/generate-crud-agent.sh --help
```

---

## Prerequisites

Before running the generator, ensure:

1. **Domain layer exists**:
   - JPA Entity (`[entity]/domain/[Entity].java`)
   - Repository (`[entity]/repository/[Entity]Repository.java`)
   - Service with CRUD methods (`[entity]/service/[Entity]Service.java`)
   - DTOs (`[Entity]Response`, `Create[Entity]Request`, `Update[Entity]Request`)

2. **Service supports partial updates**:
   ```java
   private void applyRequest(Entity entity, UpdateRequest request) {
       if (request.getField1() != null) entity.setField1(request.getField1());
       if (request.getField2() != null) entity.setField2(request.getField2());
       // Only updates non-null fields
   }
   ```

3. **Tools installed**:
   - Bash 4.0+
   - Standard Unix tools (sed, awk, grep)

---

## Interactive Prompts

### [1/8] Entity Name (Singular)

**Prompt**: `Entity name (singular, PascalCase):`

**Format**: PascalCase (first letter uppercase, no spaces)

**Examples**:
- `Cliente` (Client)
- `Propiedad` (Property)
- `Contrato` (Contract)
- `Pago` (Payment)

**Validation**: Must start with uppercase letter, alphanumeric only

---

### [2/8] Entity Name (Plural)

**Prompt**: `Entity name (plural, PascalCase):`

**Format**: PascalCase

**Examples**:
- `Clientes`
- `Propiedades`
- `Contratos`
- `Pagos`

**Validation**: Must start with uppercase letter, alphanumeric only

---

### [3/8] Package Name

**Prompt**: `Package name (lowercase):`

**Format**: lowercase, no spaces

**Examples**:
- `cliente`
- `propiedad`
- `contrato`
- `pago`

**Validation**: Must be lowercase, alphanumeric only

**Note**: This should match your existing domain package structure

---

### [4/8] English Name (Singular)

**Prompt**: `Entity name in English (singular):`

**Format**: Free text

**Examples**:
- `Client`
- `Property`
- `Contract`
- `Payment`

**Purpose**: Used in comments and documentation

---

### [5/8] English Name (Plural)

**Prompt**: `Entity name in English (plural):`

**Format**: Free text

**Examples**:
- `Clients`
- `Properties`
- `Contracts`
- `Payments`

**Purpose**: Used in agent instructions

---

### [6/8] Entity Fields

**Prompt**: `Enter entity fields (comma-separated):`

**Format**: `fieldName:Type:required,fieldName2:Type:optional`

**Examples**:

```
nombre:String:true,email:String:false,telefono:String:false
```

```
nombre:String:true,tipoCliente:String:true,rfc:String:false,direccion:String:false
```

```
direccion:String:true,tipo:String:true,numHabitaciones:Integer:false,precio:Double:true
```

**Field Format**:
- `fieldName` - camelCase field name
- `Type` - Java type (String, Integer, Long, Double, Boolean, etc.)
- `required` - `true`/`yes`/`y`/`1` for required, anything else for optional

**Important**:
- Fields should match your DTO constructor parameters
- Order should match the order in your CreateRequest DTO
- Required fields will be marked in agent instructions

---

### [7/8] Display Field

**Prompt**: `Key field for display (used in lists):`

**Format**: Single field name

**Examples**:
- `nombre` - For displaying entity name
- `email` - For user-focused entities
- `rfc` - For business entities
- `direccion` - For location-based entities

**Purpose**: This field will be featured in agent responses when listing entities

---

### [8/8] Generate Unit Tests

**Prompt**: `Generate unit tests? (y/n):`

**Options**:
- `y` - Add reminder to generate unit tests
- `n` - Skip unit test reminder

**Note**: Currently, this is a reminder only. Unit test generation templates coming soon!

---

## Configuration Summary

Before generating files, the script shows a summary:

```
Configuration Summary
────────────────────────────────────────────────────

Entity Configuration:
  Entity (singular):     Cliente
  Entity (plural):       Clientes
  Package:               cliente
  English (singular):    Client
  English (plural):      Clients
  Display field:         nombre

Fields:
  • nombre (String, required)
  • tipoCliente (String, required)
  • email (String, optional)
  • telefono (String, optional)
  • rfc (String, optional)

Options:
  Generate tests:        y

Is this correct? (y/n):
```

Review carefully and confirm with `y` to proceed.

---

## Generated Files

### 1. Tool Class

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/tools/[Entity]Tool.java`

**Contains**:
- All 5 CRUD methods:
  - `listAll[Entity]s()` - List all entities
  - `get[Entity]ById(Integer id)` - Get by ID
  - `create[Entity](...)` - Create with all fields
  - `update[Entity](Integer id, ...)` - Update (partial)
  - `delete[Entity](Integer id)` - Delete
- Comprehensive `@Schema` annotations
- Error handling with try-catch
- Map-based return types for ADK

**Example**:
```java
@Component
public class ClienteTool {

  private final ClienteService clienteService;

  @Schema(description = "List all clients in the system")
  public Map<String, Object> listAllClientes() {
    // ... implementation
  }

  // ... other CRUD methods
}
```

---

### 2. Agent Class

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/[Entity]Agent.java`

**Contains**:
- LlmAgent configuration with Gemini 2.0 Flash
- Comprehensive instruction prompt including:
  - Operation descriptions
  - Required vs optional fields
  - Partial update guidelines
  - Response formatting rules
  - Example interactions
- All 5 tools registered
- Spring component annotation

**Example**:
```java
@Component
public class ClienteAgent {

  public static final String ROOT_AGENT = "cliente-assistant";

  private void initializeAgent() {
    this.agent = LlmAgent.builder()
        .name(ROOT_AGENT)
        .model("gemini-2.0-flash")
        .instruction(buildInstruction())
        .tools(
            FunctionTool.create(clienteTool, "listAllClientes"),
            // ... other tools
        )
        .build();
  }
}
```

---

## Manual Steps Required

The generator creates the core classes, but some steps require manual completion:

### Step 1: Add Bean to AgentConfig

**File**: `src/main/java/com/inmobiliaria/gestion/agent/config/AgentConfig.java`

The script will output the exact code to add:

```java
@Bean
public InMemoryRunner clienteAgentRunner(ClienteAgent clienteAgent) {
  return new InMemoryRunner(clienteAgent.getAgent());
}
```

**Where to add**: After existing `@Bean` methods in `AgentConfig.java`

---

### Step 2: Create Controller

**Refer to**: `docs/AGENT-DEVELOPMENT-GUIDE.md` - Step 5

Create a controller at:
`src/main/java/com/inmobiliaria/gestion/agent/controller/[Entity]AgentController.java`

**Options**:
- **Separate controller** - One endpoint per agent (`/api/agent/cliente/chat`)
- **Unified controller** - Single endpoint with routing (`/api/agent/chat`)

See the guide for complete controller templates.

---

### Step 3: Create Integration Test Script

**Refer to**: `docs/AGENT-DEVELOPMENT-GUIDE.md` - Step 7 (Integration Testing)

Create a test script at:
`scripts/test-agent_[entity].sh`

The guide provides a complete bash test script template with:
- List, create, get, update, delete tests
- Partial update verification
- Delete confirmation workflow
- Error handling tests
- Session management

---

### Step 4: Create Unit Tests (Optional)

**Refer to**: `docs/AGENT-DEVELOPMENT-GUIDE.md` - Step 6

Create unit tests at:
`src/test/java/com/inmobiliaria/gestion/agent/tools/[Entity]ToolTest.java`

Use Mockito to test all Tool methods.

---

## Field Descriptions

The generator automatically provides smart descriptions for common field names:

| Field Name | Auto-Generated Description |
|------------|---------------------------|
| `nombre` | Full name |
| `email`, `correo` | Email address |
| `telefono`, `phone` | Phone number |
| `rfc` | Mexican tax ID (RFC), max 13 characters |
| `direccion`, `address` | Physical address |
| `fecha`, `date` | Date |
| `descripcion`, `description` | Description |
| `tipo`, `type` | Type or category |
| `precio`, `price` | Price amount |
| *other* | Value for [fieldName] |

These descriptions appear in `@Schema` annotations and help the LLM understand field purposes.

---

## Examples

### Example 1: Client Entity

**Input**:
```
Entity (singular): Cliente
Entity (plural): Clientes
Package: cliente
English (singular): Client
English (plural): Clients
Fields: nombre:String:true,tipoCliente:String:true,email:String:false,telefono:String:false,rfc:String:false
Display field: nombre
Generate tests: y
```

**Generated**:
- `ClienteTool.java` with methods: `listAllClientes()`, `getClienteById()`, `createCliente()`, `updateCliente()`, `deleteCliente()`
- `ClienteAgent.java` with instruction mentioning required fields: nombre, tipoCliente
- Instructions for adding `clienteAgentRunner` bean

---

### Example 2: Property Entity

**Input**:
```
Entity (singular): Propiedad
Entity (plural): Propiedades
Package: propiedad
English (singular): Property
English (plural): Properties
Fields: direccion:String:true,tipo:String:true,numHabitaciones:Integer:false,numBanos:Integer:false,precio:Double:true,disponible:Boolean:true
Display field: direccion
Generate tests: y
```

**Generated**:
- `PropiedadTool.java` with all CRUD operations
- `PropiedadAgent.java` with instructions about required fields
- Smart descriptions for `direccion` (Physical address) and `precio` (Price amount)

---

### Example 3: Contract Entity

**Input**:
```
Entity (singular): Contrato
Entity (plural): Contratos
Package: contrato
English (singular): Contract
English (plural): Contracts
Fields: clienteId:Long:true,propiedadId:Long:true,fechaInicio:String:true,fechaFin:String:true,montoMensual:Double:true,estado:String:true
Display field: clienteId
Generate tests: n
```

**Generated**:
- `ContratoTool.java` with relationship fields (clienteId, propiedadId)
- `ContratoAgent.java` with complex field requirements
- No unit test reminder

---

## Troubleshooting

### Issue: "Not in project root"

**Solution**: Run the script from the project root directory where `pom.xml` exists

```bash
cd /path/to/inmobiliaria/backend
./scripts/generate-crud-agent.sh
```

---

### Issue: "Invalid format" for entity name

**Solution**: Ensure PascalCase format:
- ✅ `Cliente` - Correct
- ✅ `Propiedad` - Correct
- ❌ `cliente` - Wrong (lowercase)
- ❌ `Cliente_Test` - Wrong (underscore)
- ❌ `Cliente Test` - Wrong (space)

---

### Issue: Fields parsing error

**Solution**: Check field format:
- Use commas to separate fields
- Use colons to separate name:type:required
- No spaces around delimiters

**Correct**:
```
nombre:String:true,email:String:false
```

**Incorrect**:
```
nombre : String : true, email : String : false
```

---

### Issue: Generated code doesn't compile

**Possible causes**:
1. Domain layer doesn't exist yet (create entity, repository, service first)
2. DTO constructor parameters don't match field list
3. Field types don't match DTO types

**Solution**: Verify domain layer exists and field definitions match DTOs exactly

---

### Issue: Service doesn't support partial updates

**Solution**: Update your service's `update()` method to only set non-null fields:

```java
private void applyRequest(Entity entity, UpdateRequest request) {
    if (request.getField1() != null) {
        entity.setField1(request.getField1());
    }
    if (request.getField2() != null) {
        entity.setField2(request.getField2());
    }
    // ... for all fields
}
```

---

## Advanced Usage

### Dry Run

Preview what will be generated without creating files:

```bash
./scripts/generate-crud-agent.sh --dry-run
```

**Output**:
```
Would create:
  ✓ src/main/java/.../agent/tools/ClienteTool.java
  ✓ src/main/java/.../agent/ClienteAgent.java

Would update:
  ~ src/main/java/.../agent/config/AgentConfig.java

Dry run complete!
```

---

### Non-Interactive Mode (Coming Soon)

Future enhancement will support command-line arguments:

```bash
./scripts/generate-crud-agent.sh \
  --entity Cliente \
  --plural Clientes \
  --package cliente \
  --fields "nombre:String:true,email:String:false"
```

---

## Architecture

### Script Structure

```
generate-crud-agent.sh
├── Configuration
│   ├── Paths and package base
│   ├── Color definitions
│   └── Options parsing
├── Helper Functions
│   ├── Validation (PascalCase, package names)
│   ├── Prompting with validation
│   ├── Case conversion utilities
│   └── Field parsing
├── Code Generation
│   ├── generate_tool_class()
│   ├── generate_agent_class()
│   └── Template substitution
└── Main Flow
    ├── Gather information (8 prompts)
    ├── Configuration summary
    ├── Generate files
    └── Show next steps
```

---

### Template System

The generator uses inline templates with placeholder substitution:

**Placeholders**:
- `${ENTITY_SINGULAR}` - Entity name (e.g., "Cliente")
- `${ENTITY_PLURAL}` - Plural form (e.g., "Clientes")
- `${ENTITY_LOWER}` - camelCase (e.g., "cliente")
- `${ENTITY_PLURAL_LOWER}` - lowercase plural (e.g., "clientes")
- `${PACKAGE_NAME}` - Package name (e.g., "cliente")
- `${TIMESTAMP}` - Generation timestamp
- `${CREATE_PARAMS}` - Generated create method parameters
- `${UPDATE_PARAMS}` - Generated update method parameters
- Field-specific placeholders

**Substitution**: Uses `sed` and `awk` for efficient text processing

---

## Benefits

### Time Savings

- **Manual**: 2-4 hours per entity
- **With generator**: 2-5 minutes per entity
- **Savings**: ~95% time reduction

---

### Consistency

- ✅ All agents follow identical patterns
- ✅ Naming conventions enforced
- ✅ No typos or forgotten annotations
- ✅ Best practices baked in

---

### Learning Tool

Junior developers can:
- Study generated code
- Understand ADK patterns
- See annotation usage
- Learn Spring Boot integration

---

### Reduced Errors

- No manual copy-paste errors
- No forgotten method registrations
- No mismatched parameter types
- No missing error handling

---

## Limitations

### Current Limitations

1. **Controller not auto-generated** - Templates coming soon
2. **Test script not auto-generated** - Templates coming soon
3. **Unit tests not auto-generated** - Templates coming soon
4. **No validation of domain layer existence** - Assumes it exists
5. **No support for complex types** - Only primitive types and String
6. **No relationship handling** - Assumes flat entity structure

### Planned Enhancements

- [ ] Full controller generation
- [ ] Integration test script generation
- [ ] Unit test generation with Mockito
- [ ] Non-interactive mode with CLI args
- [ ] Domain layer validation
- [ ] Complex type support (List, Map, nested objects)
- [ ] Relationship field handling (ManyToOne, OneToMany)
- [ ] Custom template support

---

## Best Practices

### Before Running Generator

1. ✅ Create complete domain layer (entity, repository, service, DTOs)
2. ✅ Verify service supports partial updates
3. ✅ Test service methods with unit tests
4. ✅ Document required vs optional fields

### After Generation

1. ✅ Review generated code for correctness
2. ✅ Add bean to `AgentConfig.java`
3. ✅ Create controller (see guide)
4. ✅ Create test script (see guide)
5. ✅ Run tests to verify functionality
6. ✅ Refine agent instructions if needed

### Testing

1. ✅ Test all CRUD operations manually first
2. ✅ Create comprehensive integration test script
3. ✅ Test partial updates specifically
4. ✅ Test error handling
5. ✅ Test session management

---

## Support

### Documentation

- **This README** - Generator usage
- **AGENT-DEVELOPMENT-GUIDE.md** - Comprehensive development guide
- **README-AGENT.md** - Agent architecture
- **CLAUDE.md** - Project constitution

### Questions?

1. Check the troubleshooting section
2. Review `docs/AGENT-DEVELOPMENT-GUIDE.md`
3. Study the Inmobiliaria reference implementation
4. Review generated code comments

---

## Version History

### v1.0.0 (Current)
- ✅ Interactive prompts for entity configuration
- ✅ Tool class generation with 5 CRUD methods
- ✅ Agent class generation with instructions
- ✅ Field parsing and validation
- ✅ Smart field descriptions
- ✅ Dry run mode
- ✅ PascalCase and package name validation
- ✅ Comprehensive next steps guidance

### Upcoming
- 🔜 Controller generation
- 🔜 Test script generation
- 🔜 Unit test generation
- 🔜 Non-interactive CLI mode

---

## License

Part of the Inmobiliaria Management System project.

---

**Happy Generating!** 🚀
