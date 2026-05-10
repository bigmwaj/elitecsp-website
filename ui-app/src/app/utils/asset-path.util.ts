export const ASSETS_FOLDER = 'assets';

export function assetPath(relativePath: string): string {
  const normalizedPath = relativePath.replace(/^\/+/, '');
  return `${ASSETS_FOLDER}/${normalizedPath}`;
}
