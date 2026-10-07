# Operations guide

How to configure, run, back up, upgrade, and troubleshoot the jpassvault sync
server.

## Configuration

All settings are supplied through the environment (systemd `EnvironmentFile` or a
container `env_file`). Spring Boot relaxed binding maps them to properties.

| Variable | Default | Meaning |
|---|---|---|
| `JPASSVAULT_SECRET` | — (required) | API secret. Startup fails if empty or equal to the old committed placeholder. |
| `SERVER_ADDRESS` | `0.0.0.0` | Listen address. Bind loopback when a reverse proxy fronts the service. |
| `SERVER_PORT` | `9393` | Listen port. |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:file:./maindb` | H2 database location. Point it at the persistent, owner-only data directory. |
| `APP_PROPERTIES_ALLOW_PLAIN_HTTP` | `false` | Acknowledges cleartext HTTP. Set only behind a trusted TLS proxy; otherwise startup fails. |
| `APP_PROPERTIES_TRUST_PROXY_HEADERS` | `false` | Trust `X-Forwarded-For`/`Forwarded` from a known proxy. Uses the right-most hop. |
| `APP_PROPERTIES_MAX_PAYLOAD_SIZE` | `10485760` | Maximum stored vault payload size. |

Keep the environment file owner-only (`0600`): it holds the API secret.

## TLS

The server refuses to start without TLS unless
`APP_PROPERTIES_ALLOW_PLAIN_HTTP=true`. Two supported shapes:

- **Terminate at a proxy (recommended).** Run a TLS-terminating reverse proxy in
  front, bind the server to loopback, and set `APP_PROPERTIES_ALLOW_PLAIN_HTTP=true`
  (the proxy hop is trusted and local). Set
  `APP_PROPERTIES_TRUST_PROXY_HEADERS=true` only if you need the real client
  address in logs.
- **Terminate in-process.** Configure `server.ssl.key-store` (and
  `server.ssl.enabled=true`); the plain-HTTP acknowledgment is then unnecessary.

## systemd

```sh
sudo deploy/install.sh build/libs/jpassvaultserver-2.0.0.jar
sudoedit /etc/jpassvaultserver.env        # set JPASSVAULT_SECRET
sudo systemctl start jpassvaultserver
sudo systemctl status jpassvaultserver
```

Data lives in `/var/lib/jpassvaultserver/data`, owned by the service user and
mode `0700`.

## Container

```sh
cp deploy/jpassvaultserver.env.example deploy/jpassvaultserver.env
# edit deploy/jpassvaultserver.env (set JPASSVAULT_SECRET)
docker compose up -d
```

Data lives on the `jpassvault-data` volume mounted at `/app/data`.

## Health and readiness

`GET /check` returns `{"check":"ok"}` with a valid `token` header. Both the
systemd unit and the container health check use it. A response other than
`200 {"check":"ok"}` means the service is not ready.

## Backup and restore

The database is the single H2 file `maindb.mv.db` under the data directory.
Always stop the service gracefully before copying (`systemctl stop` /
`docker compose stop`): a hard kill can lose the most recent writes, whereas a
graceful stop flushes them.

Backup:

```sh
sudo systemctl stop jpassvaultserver          # or: docker compose stop
sudo cp -a /var/lib/jpassvaultserver/data /var/backups/jpassvault-$(date +%F)
sudo systemctl start jpassvaultserver
```

Restore:

```sh
sudo systemctl stop jpassvaultserver
sudo rm -rf /var/lib/jpassvaultserver/data
sudo cp -a /var/backups/jpassvault-<date> /var/lib/jpassvaultserver/data
sudo chown -R jpassvaultserver:jpassvaultserver /var/lib/jpassvaultserver/data
sudo chmod 0700 /var/lib/jpassvaultserver/data
sudo systemctl start jpassvaultserver
```

Verify with `GET /files/last` that the expected record is present.

## Upgrade and rollback

Upgrade:

1. Back up the data directory.
2. Install the new jar/image.
3. Start and verify `GET /check` and `GET /files/last`.
4. If verification fails, roll back.

Rollback:

1. Stop the service.
2. Restore the previous jar/image version.
3. Start; the database schema is compatible with the previous version.

## Troubleshooting

- **Startup fails: "No API secret configured"** — set `JPASSVAULT_SECRET`.
- **Startup fails: "previously committed placeholder value"** — choose a new secret.
- **Startup fails: "Refusing to start without TLS"** — configure TLS or set the
  plain-HTTP acknowledgment behind a trusted proxy.
- **`401` on every request** — the client's `token` header does not match
  `JPASSVAULT_SECRET`.
- **`429 Too Many Requests`** — too many failed authentication attempts from the
  source; wait for the window to elapse or fix the token. The limiter is
  in-memory and per-instance.
- **Data directory permissions** — should be `0700` with `maindb.mv.db` `0600`;
  the service re-applies this at startup.
