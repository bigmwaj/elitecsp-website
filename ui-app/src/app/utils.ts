export class Utils {
  static parseDate(value: unknown, label: string): Date | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date && !Number.isNaN(value.getTime())) {
      return value;
    }

    if (typeof value === 'string') {
      const parsedDate = new Date(value);
      if (!Number.isNaN(parsedDate.getTime())) {
        return parsedDate;
      }
    }

    throw new Error(`Invalid ${label}`);
  }

  static parseJson<T>(value: string, label: string): T {
    try {
      return JSON.parse(value) as T;
    } catch {
      throw new Error(`Failed to parse ${label}`);
    }
  }
}