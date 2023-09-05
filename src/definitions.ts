export interface SolanaMobileWalletAdapterModule {
  startSession(options: { config?: any }): Promise<void>;
  invoke(options: { method: string, params: any }): Promise<void>;
  endSession(): Promise<void>;
}
