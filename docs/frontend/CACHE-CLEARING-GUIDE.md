# Browser Cache Clearing Guide

**Issue:** The new CRUD interface with hamburger menu (☰) may not be visible due to browser caching.

**Solution:** Force your browser to load the latest version using one of the methods below.

---

## Quick Fix: Hard Refresh ⚡ (Recommended)

This is the fastest way to clear cache for the current page:

### On Mac:
```
⌘ + Shift + R
```
or
```
⌘ + Option + R
```

### On Windows/Linux:
```
Ctrl + Shift + R
```
or
```
Ctrl + F5
```

**After hard refresh, navigate to:** http://localhost:8080/dashboard

---

## Method 1: Developer Tools (Chrome/Edge)

1. **Open the page:** http://localhost:8080
2. **Right-click** on the page
3. **Select "Inspect"** or press `F12`
4. **Right-click the refresh button** (next to the address bar)
5. **Select:** "Empty Cache and Hard Reload"

---

## Method 2: Clear Browsing Data (Chrome)

1. Press `⌘ + Shift + Delete` (Mac) or `Ctrl + Shift + Delete` (Windows)
2. Select **"Cached images and files"**
3. Time range: **"Last hour"** (or "All time" for complete clear)
4. Click **"Clear data"**
5. Refresh the page

---

## Method 3: Clear Browsing Data (Firefox)

1. Press `⌘ + Shift + Delete` (Mac) or `Ctrl + Shift + Delete` (Windows)
2. Select **"Cache"**
3. Time range: **"Everything"**
4. Click **"Clear Now"**
5. Refresh the page

---

## Method 4: Incognito/Private Mode

Open the application in a private window:

### Chrome/Edge:
```
⌘ + Shift + N (Mac)
Ctrl + Shift + N (Windows)
```

### Firefox:
```
⌘ + Shift + P (Mac)
Ctrl + Shift + P (Windows)
```

Then navigate to: http://localhost:8080

---

## Method 5: Disable Cache in DevTools (For Development)

Keep cache disabled while testing:

1. Open DevTools (`F12`)
2. Go to **"Network"** tab
3. Check **"Disable cache"** checkbox
4. Keep DevTools open while browsing

---

## Verification: How to Know It Worked

After clearing cache, you should see:

✅ **Hamburger menu icon (☰)** in the top-left corner of the header
✅ Clicking it opens a **navigation drawer** with menu items:
   - Panel Principal
   - Inmobiliarias
   - Propiedades
   - Personas
   - Contratos
   - Chat con Agentes

✅ **Dashboard shows real data** (not 0 for all counts)

---

## Still Not Working?

If you still don't see the hamburger menu after trying all methods:

### 1. Check Browser Console for Errors
1. Press `F12` to open DevTools
2. Go to **Console** tab
3. Look for red error messages
4. Take a screenshot and share with the development team

### 2. Check Network Tab
1. Press `F12` to open DevTools
2. Go to **Network** tab
3. Refresh the page
4. Look for failed requests (red status codes)

### 3. Verify Server is Running
Check that the backend server is running on http://localhost:8080:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 4. Check File Timestamps
Verify the frontend build is recent:
```bash
ls -la src/main/resources/static/
```

The files should have recent timestamps (today's date).

---

## Technical Details (For Developers)

### Cache Control Headers Added

The following cache configuration has been implemented:

**WebConfig.java:**
```java
// Static assets (CSS, JS) cached for 1 hour
registry.addResourceHandler("/assets/**")
    .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());

// index.html NEVER cached
registry.addResourceHandler("/", "/index.html")
    .setCacheControl(CacheControl.noCache().noStore().mustRevalidate());
```

**Vite Build:**
- All assets include content-based hash in filename
- Example: `index-428a77f5.css`, `index-45aa6e1d.js`
- File hash changes when content changes
- Browser automatically loads new version

### Why Caching Was an Issue

1. **Old Version Cached:** Browser stored the previous version without hamburger menu
2. **No Cache Headers:** Original setup had no explicit cache control
3. **Aggressive Caching:** Browsers aggressively cache static files for performance

### What We Fixed

1. ✅ Added cache control headers to prevent index.html caching
2. ✅ Content-based hashing ensures new versions load
3. ✅ Assets cache for 1 hour (good for performance)
4. ✅ HTML never cached (always fresh)

---

## Future Prevention

For future updates, the following best practices are now in place:

1. **Content Hashing:** All assets have unique filenames based on content
2. **Cache Control:** Proper HTTP headers prevent stale pages
3. **Service Workers:** Consider adding for better cache management
4. **Versioning:** Could add version number to help track updates

---

## Quick Reference Card

Print this for your team:

```
┌─────────────────────────────────────────┐
│   CLEAR BROWSER CACHE - QUICK GUIDE    │
├─────────────────────────────────────────┤
│                                         │
│  Mac:          ⌘ + Shift + R           │
│  Windows:      Ctrl + Shift + R         │
│                                         │
│  Or:                                    │
│  1. Open DevTools (F12)                 │
│  2. Right-click refresh button          │
│  3. "Empty Cache and Hard Reload"       │
│                                         │
│  After refresh:                         │
│  ✓ Look for ☰ menu in top-left         │
│  ✓ Navigate to /dashboard               │
│                                         │
└─────────────────────────────────────────┘
```

---

**Last Updated:** October 30, 2025
**Issue:** Hamburger menu not visible
**Status:** ✅ Fixed with cache control headers
**Action:** Users must clear cache to see updates
