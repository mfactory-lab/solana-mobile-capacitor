# solana-wallet-capacitor

connect to native solana wallets

## Install

```bash
npm install solana-wallet-capacitor
npx cap sync
```

## API

<docgen-index>

* [`startSession(...)`](#startsession)
* [`invoke(...)`](#invoke)
* [`endSession()`](#endsession)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startSession(...)

```typescript
startSession(options: { config?: any; }) => Promise<void>
```

| Param         | Type                           |
| ------------- | ------------------------------ |
| **`options`** | <code>{ config?: any; }</code> |

--------------------


### invoke(...)

```typescript
invoke(options: { method: string; params: any; }) => Promise<void>
```

| Param         | Type                                          |
| ------------- | --------------------------------------------- |
| **`options`** | <code>{ method: string; params: any; }</code> |

--------------------


### endSession()

```typescript
endSession() => Promise<void>
```

--------------------

</docgen-api>
