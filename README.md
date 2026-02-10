## 🍋 Lemon Parent

Lemon is an opinionated Spring Boot parent that provides secure, sane, production-ready defaults.
If something is risky, slow, or hard to operate. Lemon does not enable it by default.

### The Rules of Lemon

1. **Secure by default**
   - Nothing is implicitly public
   - Sensible security and encryption defaults
   - Secrets never live in code

2. **Observable by default**
   - Health, metrics, and traces always available
   - Structured and consistent logs
   - Production issues diagnosable without redeploying

3. **Fail fast, fail clear**
   - Inputs validated at the edge
   - Consistent, client-friendly errors
   - Misconfiguration breaks at startup, not runtime