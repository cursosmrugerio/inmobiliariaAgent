# Frontend CRUD Testing Plan

**Date:** October 30, 2025
**Application:** Sistema de Gestión Inmobiliaria - CRUD Interface
**URL:** http://localhost:8080

---

## Prerequisites

### 1. Clear Browser Cache
Before testing, ensure you're viewing the latest version:

**Chrome/Edge:**
- Press `Ctrl + Shift + Delete` (Windows) or `⌘ + Shift + Delete` (Mac)
- Select "Cached images and files"
- Click "Clear data"
- Or use Hard Refresh: `Ctrl + Shift + R` (Windows) or `⌘ + Shift + R` (Mac)

**Firefox:**
- Press `Ctrl + Shift + Delete` (Windows) or `⌘ + Shift + Delete` (Mac)
- Select "Cache"
- Click "Clear Now"

### 2. Test Credentials
```
Email: admin@test.com
Password: admin123
```

---

## Test Suite 1: Navigation & Layout

### Test 1.1: Header with Hamburger Menu
**Expected Result:** ✅ Header should display:
- ☰ Hamburger menu icon (top-left)
- "Sistema de Gestión Inmobiliaria" title
- Language switcher (Español/English)
- Welcome message with username
- "CERRAR SESIÓN" button

**Steps:**
1. Navigate to http://localhost:8080
2. Login with test credentials
3. Verify header is visible with all elements

**Status:** ⚠️ **ISSUE DETECTED** - Hamburger menu may not be visible due to browser cache

**Solution:** Hard refresh browser (⌘ + Shift + R)

---

### Test 1.2: Navigation Drawer
**Expected Result:** ✅ Clicking hamburger menu should open a drawer with:
- Panel Principal (Dashboard)
- Inmobiliarias
- Propiedades
- Personas
- Contratos
- --- (divider) ---
- Chat con Agentes

**Steps:**
1. Click the ☰ hamburger menu icon
2. Verify all menu items are present
3. Verify active route is highlighted
4. Click outside to close drawer

**Pass Criteria:**
- ✅ Drawer opens smoothly
- ✅ All menu items visible
- ✅ Active route highlighted in blue
- ✅ Drawer closes when clicking outside

---

### Test 1.3: Route Navigation
**Expected Result:** ✅ Clicking menu items navigates to correct pages

**Steps:**
1. Open navigation drawer
2. Click "Panel Principal" → Should go to /dashboard
3. Click "Inmobiliarias" → Should go to /inmobiliarias
4. Click "Propiedades" → Should go to /propiedades
5. Click "Personas" → Should go to /personas
6. Click "Chat con Agentes" → Should go to /chat

**Pass Criteria:**
- ✅ URL changes correctly
- ✅ Page content changes
- ✅ No errors in browser console

---

## Test Suite 2: Dashboard

### Test 2.1: Dashboard Stats
**Expected Result:** ✅ Dashboard displays 4 stat cards with real counts

**Steps:**
1. Navigate to http://localhost:8080/dashboard
2. Verify 4 cards are displayed:
   - Inmobiliarias (blue icon)
   - Propiedades (green icon)
   - Personas (orange icon)
   - Contratos (red icon)

**Pass Criteria:**
- ✅ Shows actual count (not 0 if data exists)
- ✅ Icons displayed with correct colors
- ✅ Responsive layout (4 columns on desktop, 2 on tablet, 1 on mobile)

**Current Status:** ✅ Fixed - Dashboard now fetches real data

---

## Test Suite 3: Inmobiliarias CRUD

### Test 3.1: List View
**Expected Result:** ✅ Table displays all agencies

**Steps:**
1. Navigate to /inmobiliarias
2. Verify DataGrid is displayed
3. Check columns: ID, Nombre, RFC, Nombre Contacto, Correo, Teléfono, Acciones

**Pass Criteria:**
- ✅ Data loads within 2 seconds
- ✅ All columns visible
- ✅ Action buttons (Edit, Delete) present
- ✅ Pagination controls visible

---

### Test 3.2: Search Functionality
**Expected Result:** ✅ Search filters results in real-time

**Steps:**
1. On /inmobiliarias page
2. Type text in search box
3. Wait for debounce (300ms)
4. Verify filtered results

**Pass Criteria:**
- ✅ Search works across all fields
- ✅ Debouncing prevents excessive filtering
- ✅ Results update smoothly
- ✅ Clear search shows all results

---

### Test 3.3: Create New Inmobiliaria
**Expected Result:** ✅ Successfully creates new record

**Steps:**
1. Click "NUEVA INMOBILIARIA" button
2. Fill form:
   - Nombre: "Test Agency"
   - RFC: "TEST123456"
   - Nombre Contacto: "John Doe"
   - Correo: "john@test.com"
   - Teléfono: "1234567890"
3. Click "GUARDAR"

**Pass Criteria:**
- ✅ Form opens in modal dialog
- ✅ All fields have labels
- ✅ Validation works (try submitting empty)
- ✅ Success message appears
- ✅ New record visible in table
- ✅ Dialog closes

---

### Test 3.4: Edit Inmobiliaria
**Expected Result:** ✅ Successfully updates record

**Steps:**
1. Click Edit icon (pencil) on any row
2. Modify "Nombre" field
3. Click "GUARDAR"

**Pass Criteria:**
- ✅ Form pre-populated with current values
- ✅ Changes saved successfully
- ✅ Success message appears
- ✅ Table updates with new values

---

### Test 3.5: Delete Inmobiliaria
**Expected Result:** ✅ Successfully deletes record with confirmation

**Steps:**
1. Click Delete icon (trash) on any row
2. Confirm deletion in dialog
3. Verify record removed

**Pass Criteria:**
- ✅ Confirmation dialog appears
- ✅ Can cancel deletion
- ✅ Success message after deletion
- ✅ Record removed from table

---

### Test 3.6: Form Validation
**Expected Result:** ✅ Validation prevents invalid data

**Steps:**
1. Click "NUEVA INMOBILIARIA"
2. Try to submit empty form
3. Enter invalid email format
4. Verify error messages

**Pass Criteria:**
- ✅ Required field errors shown
- ✅ Email validation works
- ✅ Cannot submit invalid form
- ✅ Error messages in Spanish/English

---

## Test Suite 4: Propiedades CRUD

### Test 4.1: List View with Relationships
**Expected Result:** ✅ Table shows properties with agency names

**Steps:**
1. Navigate to /propiedades
2. Verify columns: ID, Nombre, Tipo, Dirección, Observaciones, Inmobiliaria

**Pass Criteria:**
- ✅ Inmobiliaria name displayed (not just ID)
- ✅ Tipo enum translated (CASA → Casa, etc.)
- ✅ Observaciones truncated with tooltip
- ✅ All action buttons present

---

### Test 4.2: Filter by Inmobiliaria
**Expected Result:** ✅ Dropdown filters properties

**Steps:**
1. On /propiedades page
2. Open "Filtrar por Inmobiliaria" dropdown
3. Select an agency
4. Verify only properties for that agency shown

**Pass Criteria:**
- ✅ Dropdown populated with agencies
- ✅ Filter works immediately
- ✅ "Todas" option shows all properties
- ✅ Filter persists during search

---

### Test 4.3: Create Property
**Expected Result:** ✅ Creates property linked to agency

**Steps:**
1. Click "NUEVA PROPIEDAD"
2. Fill form:
   - Nombre: "Test Property"
   - Tipo: Select "CASA"
   - Dirección: "123 Test St"
   - Observaciones: "Test notes"
   - Inmobiliaria: Select from dropdown
3. Click "GUARDAR"

**Pass Criteria:**
- ✅ Tipo dropdown shows translated labels
- ✅ Inmobiliaria dropdown loads agencies
- ✅ Observaciones is multiline (4 rows)
- ✅ All validations work
- ✅ Success message appears

---

### Test 4.4: Edit Property
**Expected Result:** ✅ Updates property successfully

**Steps:**
1. Click Edit on any property
2. Change Tipo from CASA to DEPARTAMENTO
3. Click "GUARDAR"

**Pass Criteria:**
- ✅ Tipo dropdown pre-selected
- ✅ Inmobiliaria dropdown pre-selected
- ✅ Changes saved
- ✅ Table updates

---

## Test Suite 5: Personas CRUD

### Test 5.1: List View with Smart Display
**Expected Result:** ✅ Table shows appropriate name format

**Steps:**
1. Navigate to /personas
2. Verify columns: ID, Tipo Persona, Nombre Completo, RFC, Email, Teléfono, Fecha Alta, Activo

**Pass Criteria:**
- ✅ Individuals show "Nombre Apellidos"
- ✅ Companies show "Razón Social"
- ✅ Tipo Persona translated
- ✅ Fecha Alta formatted correctly
- ✅ Activo shows colored chip (green/gray)

---

### Test 5.2: Create Persona - Individual
**Expected Result:** ✅ Creates individual contact

**Steps:**
1. Click "NUEVA PERSONA"
2. Select Tipo: "ARRENDATARIO"
3. Fill form:
   - Nombre: "Juan"
   - Apellidos: "Pérez"
   - RFC: "PEXJ800101"
   - Email: "juan@test.com"
   - Teléfono: "1234567890"
4. Click "GUARDAR"

**Pass Criteria:**
- ✅ Nombre and Apellidos fields visible
- ✅ Both fields required
- ✅ CURP field optional
- ✅ Success message
- ✅ Shows "Juan Pérez" in table

---

### Test 5.3: Create Persona - Company
**Expected Result:** ✅ Form changes for company type

**Steps:**
1. Click "NUEVA PERSONA"
2. Select Tipo: "OTRO"
3. Verify form changes:
   - Razón Social field appears
   - Nombre/Apellidos not required

**Pass Criteria:**
- ✅ Conditional fields work
- ✅ Form layout adapts
- ✅ Validation adjusts
- ✅ Success message

---

### Test 5.4: Toggle Active Status
**Expected Result:** ✅ Switch controls active status

**Steps:**
1. Edit any persona
2. Toggle "Activo" switch
3. Save
4. Verify status chip changes color

**Pass Criteria:**
- ✅ Switch works
- ✅ Status saved
- ✅ Chip color changes (green→gray or gray→green)

---

## Test Suite 6: Internationalization

### Test 6.1: Language Switching
**Expected Result:** ✅ Interface switches between Spanish and English

**Steps:**
1. On any page, click language dropdown
2. Switch from "Español" to "English"
3. Verify all text changes

**Pass Criteria:**
- ✅ Menu items translated
- ✅ Button labels translated
- ✅ Form fields translated
- ✅ Validation messages translated
- ✅ Table headers translated
- ✅ Enum values translated

---

## Test Suite 7: Responsive Design

### Test 7.1: Mobile View (< 600px)
**Expected Result:** ✅ Layout adapts for mobile

**Steps:**
1. Resize browser to 375px width (iPhone)
2. Navigate through all pages

**Pass Criteria:**
- ✅ Navigation drawer works
- ✅ Tables scroll horizontally
- ✅ Forms stack vertically
- ✅ Buttons full-width on mobile
- ✅ No horizontal overflow

---

### Test 7.2: Tablet View (600-960px)
**Expected Result:** ✅ Layout optimized for tablet

**Steps:**
1. Resize browser to 768px (iPad)
2. Navigate through all pages

**Pass Criteria:**
- ✅ Dashboard shows 2 columns
- ✅ Forms use 2-column grid
- ✅ Tables readable
- ✅ Drawer works

---

### Test 7.3: Desktop View (> 960px)
**Expected Result:** ✅ Full desktop layout

**Steps:**
1. View on 1920px desktop
2. Verify optimal spacing

**Pass Criteria:**
- ✅ Dashboard shows 4 columns
- ✅ Tables use full width
- ✅ Forms well-spaced
- ✅ All features accessible

---

## Test Suite 8: Error Handling

### Test 8.1: Network Errors
**Expected Result:** ✅ Graceful error messages

**Steps:**
1. Stop backend server
2. Try to create a record
3. Verify error message

**Pass Criteria:**
- ✅ User-friendly error message
- ✅ No application crash
- ✅ Can retry after server restart

---

### Test 8.2: Validation Errors
**Expected Result:** ✅ Clear validation feedback

**Steps:**
1. Submit form with invalid data
2. Verify error messages

**Pass Criteria:**
- ✅ Errors displayed inline
- ✅ Messages are helpful
- ✅ Fields highlighted
- ✅ Focus on first error

---

## Test Suite 9: Performance

### Test 9.1: Initial Load Time
**Expected Result:** ✅ App loads quickly

**Pass Criteria:**
- ✅ Login page < 2 seconds
- ✅ Dashboard < 3 seconds
- ✅ CRUD pages < 3 seconds

---

### Test 9.2: Search Debouncing
**Expected Result:** ✅ Smooth search experience

**Pass Criteria:**
- ✅ No lag during typing
- ✅ Results update after 300ms pause
- ✅ No excessive API calls

---

## Test Suite 10: Integration with Agent Chat

### Test 10.1: Navigate Between CRUD and Chat
**Expected Result:** ✅ Seamless transition

**Steps:**
1. Create record in CRUD interface
2. Navigate to Chat
3. Ask agent about the record
4. Verify agent can access it

**Pass Criteria:**
- ✅ Both interfaces work independently
- ✅ Shared backend data
- ✅ No conflicts

---

## Known Issues

### Issue 1: Hamburger Menu Not Visible
**Status:** ⚠️ **CRITICAL**
**Cause:** Browser cache showing old version
**Solution:** Hard refresh (⌘ + Shift + R)
**Affected:** All users on first load after update

---

### Issue 2: Dashboard Shows 0 Counts
**Status:** ✅ **FIXED**
**Cause:** Hardcoded placeholder values
**Solution:** Updated to fetch real data from API
**Version:** Fixed in latest build

---

## Test Summary Checklist

Use this checklist for complete testing:

### Navigation & Layout
- [ ] Header with hamburger menu visible
- [ ] Navigation drawer opens/closes
- [ ] All routes accessible
- [ ] Active route highlighting works

### Dashboard
- [ ] Stats load with real data
- [ ] Responsive grid layout
- [ ] Icons and colors correct

### Inmobiliarias CRUD
- [ ] List view loads data
- [ ] Search functionality works
- [ ] Create new record works
- [ ] Edit record works
- [ ] Delete with confirmation works
- [ ] Form validation works

### Propiedades CRUD
- [ ] List view with relationships
- [ ] Filter by inmobiliaria works
- [ ] Tipo enum translated
- [ ] Observaciones truncated
- [ ] Create with dropdowns works
- [ ] Edit works

### Personas CRUD
- [ ] List view with smart names
- [ ] Status chips display
- [ ] Create individual works
- [ ] Create company works
- [ ] Conditional form fields work
- [ ] Date formatting correct

### Internationalization
- [ ] Spanish → English switch works
- [ ] All text translated
- [ ] Enum values translated

### Responsive Design
- [ ] Mobile layout works
- [ ] Tablet layout works
- [ ] Desktop layout optimal

### Error Handling
- [ ] Network errors handled
- [ ] Validation errors clear
- [ ] No crashes

### Performance
- [ ] Load times acceptable
- [ ] Search is smooth
- [ ] No lag or freezing

---

## Test Report Template

After completing tests, document results:

```markdown
## Test Execution Report

**Date:** [Date]
**Tester:** [Name]
**Browser:** [Chrome/Firefox/Safari]
**Version:** [Browser Version]
**OS:** [macOS/Windows/Linux]

### Test Results
- Total Tests: [X]
- Passed: [X]
- Failed: [X]
- Blocked: [X]

### Failed Tests
1. [Test Name] - [Reason]
2. [Test Name] - [Reason]

### Screenshots
[Attach screenshots of any issues]

### Notes
[Any additional observations]
```

---

## Automated Testing (Future)

Recommend implementing:
- **Unit Tests:** Jest + React Testing Library
- **E2E Tests:** Playwright or Cypress
- **API Tests:** Jest + Supertest
- **Coverage Target:** >80%

---

**Last Updated:** October 30, 2025
**Next Review:** After any major updates
