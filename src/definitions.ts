export interface SolanaMobileWalletAdapterModule {
  echo(options: { value: string }): Promise<{ value: string }>;
  startSession(options: { config?: any }): Promise<void>;
  invoke(options: { method: string, params: any }): Promise<void>;
  endSession(): Promise<void>;
}
