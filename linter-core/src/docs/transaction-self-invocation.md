# Transactional self-invocation

**Status:** Rule specified. Receiver and annotation inspection have been verified. Method resolution and violation reporting are still to be implemented.

## Purpose

When a Spring service calls one of its own methods directly, the call bypasses Spring’s transaction proxy. In proxy mode, the called method’s `@Transactional` settings are skipped for that invocation. This matters especially for `REQUIRES_NEW`, where the developer expects the helper to start a fresh transaction. An existing transaction from the caller can still be active.

## When to flag

Report one warning at the call site when all three conditions hold:

1. The call has no explicit receiver, as in `recover(ref)`, or uses plain `this`, as in `this.recover(ref)`.
2. The call resolves to an instance method declared in the same class. If the method is overloaded, inspect the overload selected by the call.
3. The selected method is directly annotated with `org.springframework.transaction.annotation.Transactional` or `jakarta.transaction.Transactional`.

The caller may be annotated or unannotated. If the target method or annotation cannot be identified reliably, skip the call. Version 1 covers direct method annotations and excludes inherited methods, class-level annotations, and aliases for `this`.

## VaultGuard examples

| Call | Expected result | Reason |
|---|---|---|
| `updateRiskScore(trx, riskScore)` inside `calculateRiskScore` | **Flag** | In the latest `TransactionService`, this directly calls the same class’s annotated `updateRiskScore` method. |
| `getTransactionByReference(ref)` in the reconstructed recovery example | **Flag** | The reconstructed helper is declared on the same class and requests `REQUIRES_NEW`. |
| `transactionUtil.getTransactionByReference(ref)` in the committed fix | **Allow** | The lookup goes through the separate `TransactionUtil` service. |
| `transactionRepository.findByTransactionReference(...)` in the original recovery code | **Allow** | The receiver is the repository. This was the historical recovery path. |
| `validateTransaction(trx)` | **Allow** | The call is on the same object, but the selected method has no transactional annotation. |

## Example warning

The Following code is an example from one of my project 
```java
    @Transactional
    public void calculateRiskScore(Transaction trx){
        ...
        updateRiskScore(trx, riskScore); // Bypasses teh Spring Proxy, REQUIRES_NEW is ignored
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRiskScore(Transaction trx, double score){ ... }
```

> Direct call to `updateRiskScore` bypasses Spring’s transaction proxy. In proxy mode, its `@Transactional` settings are skipped for this call.