# CRUD Agent Generator - Quick Start

**Generate a complete CRUD agent in 2 minutes!**

---

## One Command

```bash
./scripts/generate-crud-agent.sh
```

---

## 8 Simple Questions

### 1. Entity Name (Singular)
```
Example: Cliente
```

### 2. Entity Name (Plural)
```
Example: Clientes
```

### 3. Package Name
```
Example: cliente
```

### 4. English Name (Singular)
```
Example: Client
```

### 5. English Name (Plural)
```
Example: Clients
```

### 6. Entity Fields
```
Format: fieldName:Type:required,field2:Type:optional

Example: nombre:String:true,email:String:false,telefono:String:false
```

### 7. Display Field
```
Example: nombre
```

### 8. Generate Tests?
```
y or n
```

---

## What You Get

✅ **Tool class** - 5 CRUD methods ready
✅ **Agent class** - Smart AI instructions
✅ **Instructions** - Exact code to add to AgentConfig

---

## Next Steps (Manual)

### 1. Add Bean to AgentConfig.java

The script shows you exactly what to add:

```java
@Bean
public InMemoryRunner [entity]AgentRunner([Entity]Agent [entity]Agent) {
  return new InMemoryRunner([entity]Agent.getAgent());
}
```

### 2. Create Controller

See `docs/AGENT-DEVELOPMENT-GUIDE.md` Step 5

### 3. Create Test Script

See `docs/AGENT-DEVELOPMENT-GUIDE.md` Step 7

### 4. Test It!

```bash
mvn spring-boot:run
./scripts/test-agent_[entity].sh
```

---

## Example Session

```bash
$ ./scripts/generate-crud-agent.sh

╔══════════════════════════════════════════════════════════╗
║     ADK CRUD Agent Generator                             ║
║     Inmobiliaria Management System                       ║
╚══════════════════════════════════════════════════════════╝

─────────────────────────────────────────────────────────
[1/8] Entity Information
─────────────────────────────────────────────────────────

Entity name (singular, PascalCase):
Example: Cliente, Propiedad, Contrato
> Cliente

─────────────────────────────────────────────────────────
[2/8] Entity Plural
─────────────────────────────────────────────────────────

Entity name (plural, PascalCase):
Example: Clientes, Propiedades, Contratos
> Clientes

─────────────────────────────────────────────────────────
[3/8] Package Name
─────────────────────────────────────────────────────────

Package name (lowercase):
Example: cliente, propiedad, contrato
> cliente

─────────────────────────────────────────────────────────
[4/8] Human-readable Name (English)
─────────────────────────────────────────────────────────

Entity name in English (singular):
Example: Client, Property, Contract
> Client

─────────────────────────────────────────────────────────
[5/8] Human-readable Name Plural (English)
─────────────────────────────────────────────────────────

Entity name in English (plural):
Example: Clients, Properties, Contracts
> Clients

─────────────────────────────────────────────────────────
[6/8] Entity Fields
─────────────────────────────────────────────────────────

Enter entity fields (comma-separated):
Format: fieldName:Type:required
Example: nombre:String:true,email:String:false,telefono:String:false
> nombre:String:true,tipoCliente:String:true,email:String:false,telefono:String:false

─────────────────────────────────────────────────────────
[7/8] Display Field
─────────────────────────────────────────────────────────

Key field for display (used in lists):
Example: nombre, email, rfc
> nombre

─────────────────────────────────────────────────────────
[8/8] Testing Options
─────────────────────────────────────────────────────────

Generate unit tests? (y/n):
> y

─────────────────────────────────────────────────────────
Configuration Summary
─────────────────────────────────────────────────────────

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

Options:
  Generate tests:        y

Is this correct? (y/n): y

─────────────────────────────────────────────────────────
Generating CRUD Agent...
─────────────────────────────────────────────────────────

Creating Tool class... ✓ Created: agent/tools/ClienteTool.java
Creating Agent class... ✓ Created: agent/ClienteAgent.java

─────────────────────────────────────────────────────────
Generation Complete!
─────────────────────────────────────────────────────────

✓ Created: Tool class
✓ Created: Agent class
⚠  Manual step required: Add bean to AgentConfig.java
⚠  Manual step required: Create controller
⚠  Manual step required: Create test script
```

---

## Common Field Examples

### Client Entity
```
nombre:String:true,tipoCliente:String:true,email:String:false,telefono:String:false,rfc:String:false
```

### Property Entity
```
direccion:String:true,tipo:String:true,numHabitaciones:Integer:false,precio:Double:true,disponible:Boolean:true
```

### Contract Entity
```
clienteId:Long:true,propiedadId:Long:true,fechaInicio:String:true,fechaFin:String:true,montoMensual:Double:true
```

### Payment Entity
```
contratoId:Long:true,monto:Double:true,fechaPago:String:true,metodoPago:String:false,estado:String:true
```

---

## Validation Rules

### Entity Names
- ✅ `Cliente` - Correct (PascalCase)
- ✅ `Propiedad` - Correct
- ❌ `cliente` - Wrong (lowercase)
- ❌ `Cliente_Test` - Wrong (underscore)

### Package Names
- ✅ `cliente` - Correct (lowercase)
- ✅ `propiedad` - Correct
- ❌ `Cliente` - Wrong (uppercase)
- ❌ `cliente-test` - Wrong (hyphen)

### Field Format
- ✅ `nombre:String:true` - Correct
- ✅ `email:String:false` - Correct
- ❌ `nombre:String` - Wrong (missing required flag)
- ❌ `nombre : String : true` - Wrong (spaces)

---

## Tips

### Before Running

1. ✅ Create domain layer first (entity, service, DTOs)
2. ✅ Make sure service supports partial updates
3. ✅ Know which fields are required vs optional

### During Generation

1. ✅ Use PascalCase for entity names
2. ✅ Use lowercase for package names
3. ✅ List fields in DTO constructor order
4. ✅ Mark required fields correctly

### After Generation

1. ✅ Review generated code
2. ✅ Add the bean to AgentConfig
3. ✅ Create controller and tests
4. ✅ Test thoroughly

---

## Dry Run Mode

Test without creating files:

```bash
./scripts/generate-crud-agent.sh --dry-run
```

Perfect for:
- Previewing output
- Verifying configuration
- Learning what will be generated

---

## Need Help?

- **Full Guide**: `docs/AGENT-DEVELOPMENT-GUIDE.md`
- **Generator Details**: `scripts/generator/README.md`
- **Architecture**: `docs/README-AGENT.md`
- **Project Standards**: `CLAUDE.md`

---

## Time Savings

- **Manual Development**: 2-4 hours per entity
- **With Generator**: 2-5 minutes per entity
- **You Save**: ~95% of boilerplate time

**Focus on business logic, not boilerplate!** 🚀
