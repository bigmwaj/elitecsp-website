/**
 * Utility function to generate asset paths for the application.
 * This helps maintain a consistent way to reference assets across the app,
 * and allows for easy updates to the assets folder structure if needed.
 * 
 * Remark: The ASSETS_FOLDER constant is modified during the build process to avoid AWS CloudFront caching issues.
 */
export const ASSETS_FOLDER = 'assets';

export function assetPath(relativePath: string): string {
  const normalizedPath = relativePath.replace(/^\/+/, '');
  return `${ASSETS_FOLDER}/${normalizedPath}`;
}
