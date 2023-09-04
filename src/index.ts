import { registerPlugin } from '@capacitor/core';

import type { SolanaMobileWalletAdapterModule } from './definitions';

const SolanaMobileWalletAdapter = registerPlugin<SolanaMobileWalletAdapterModule>('SolanaMobileWalletAdapter', {
  web: () => import('./web').then(m => new m.SolanaMobileWalletAdapterWeb()),
});

export * from './definitions';
export { SolanaMobileWalletAdapter };
