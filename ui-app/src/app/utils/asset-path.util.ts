export const ASSETS_FOLDER = 'static';

export function assetPath(relativePath: string): string {
  const normalizedPath = relativePath.replace(/^\/+/, '');
  return `${ASSETS_FOLDER}/${normalizedPath}`;
}
