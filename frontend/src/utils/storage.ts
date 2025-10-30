export const storage = {
  get: <T>(key: string): T | null => {
    try {
      const item = localStorage.getItem(key);
      if (!item) {
        return null;
      }
      // Try to parse as JSON, but if it fails (e.g., it's a plain string), return as-is
      try {
        return JSON.parse(item) as T;
      } catch {
        // If parsing fails, return the raw string (for backward compatibility)
        return item as T;
      }
    } catch (error) {
      console.error(`Failed to get storage item for key "${key}"`, error);
      return null;
    }
  },

  set: <T>(key: string, value: T): void => {
    try {
      // Store strings directly without JSON serialization to avoid double-quoting
      if (typeof value === 'string') {
        localStorage.setItem(key, value);
      } else {
        const serializedValue = JSON.stringify(value);
        localStorage.setItem(key, serializedValue);
      }
    } catch (error) {
      console.error(`Failed to serialize storage item for key "${key}"`, error);
    }
  },

  remove: (key: string): void => {
    localStorage.removeItem(key);
  },

  clear: (): void => {
    localStorage.clear();
  },
};

export default storage;
