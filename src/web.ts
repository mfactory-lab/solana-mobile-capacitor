import { WebPlugin } from '@capacitor/core';

import type {
  SolanaMobileWalletAdapterModule,
} from './definitions';

export class SolanaMobileWalletAdapterWeb extends WebPlugin implements SolanaMobileWalletAdapterModule {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('[ExamplePlugin] ECHO', options);
    return options;
  }

  async startSession(options: { config: any }): Promise<void> {
    console.log('[ExamplePlugin] startSession config: ', options?.config)
    // logic here
  }

  async invoke(options: { method: string, params: any }): Promise<void> {
    console.log('[ExamplePlugin] invoke method: ', options?.method)
    console.log('[ExamplePlugin] invoke params: ', options?.params)
    // logic here
  }

  async endSession(): Promise<void> {
    console.log('[ExamplePlugin] endSession')
    // logic here
  }
}
