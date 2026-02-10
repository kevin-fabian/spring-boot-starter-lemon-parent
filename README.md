## 🍋 Lemon Parent

Lemon is an opinionated Spring Boot parent that provides secure, sane, production-ready defaults.

If something is risky, slow, or hard to operate — Lemon does not enable it by default.

### The Rules of Lemon

#### 1. Secure by default

- Nothing is implicitly public
- Sensible security and encryption defaults
- Secrets must never live in code

#### 2. Observable by default

- Health, metrics, and traces are always available
- Logs are structured and consistent
- Production issues should be diagnosable without redeploying

#### 3. Fail fast, fail clear

- Inputs are validated at the edge
- Errors are consistent and client-friendly
- Misconfiguration should break at startup, not runtime