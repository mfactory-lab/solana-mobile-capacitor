# solana-wallet-capacitor

connect to native solana wallets

## Install

```bash
npm install solana-wallet-capacitor
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`startSession(...)`](#startsession)
* [`invoke(...)`](#invoke)
* [`endSession()`](#endsession)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


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
