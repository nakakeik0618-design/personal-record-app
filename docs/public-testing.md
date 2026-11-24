# 一時公開（友人テスト用）の手順

自分のPCで動かしている MindLog を、Cloudflare Tunnel で一時的にネットに公開する手順。
PCとサーバーを起動している間だけ使える。

## 構成

```
友人のブラウザ ──HTTPS──▶ Cloudflare ──トンネル──▶ このPCの Spring Boot（127.0.0.1:8080）──▶ MySQL（127.0.0.1:3307）
```

- HTTPS の暗号化は Cloudflare が行う。PC の中（Cloudflare → Spring Boot）は HTTP。
- React はビルドした成果物（`frontend/dist`）を Spring Boot から配信する（API設計書 1.4節）。Vite は使わない。
- Spring Boot と MySQL は、このPCからのみ接続できる（同じWi-Fiの他の機器からも接続できない）。

## 起動

```sh
# 1. MySQL
docker start mindlog-mysql

# 2. React をビルドする（画面のコードを変えたら毎回必要）
cd frontend && npm run build && cd ..

# 3. Spring Boot を公開用の設定（application-public.properties）で起動する
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=public

# 4. 別のターミナルでトンネルを作る
cloudflared tunnel --no-autoupdate --url http://127.0.0.1:8080
```

4 の出力に `https://xxxx.trycloudflare.com` が表示される。この URL を友人に渡す。
URL はトンネルを作るたびに変わる。

## 停止

トンネル（4）を `Ctrl + C` で止めると、外からは接続できなくなる。

## 公開用の設定でしていること（application-public.properties）

| 設定 | 内容 |
|---|---|
| `server.address=127.0.0.1` | トンネル以外からの接続を受け付けない |
| `server.forward-headers-strategy=framework` | Cloudflare が付けるヘッダーから「ブラウザとは HTTPS で通信している」と判断する |
| `server.servlet.session.cookie.secure=true` | ログインの Cookie を HTTPS の通信でのみ送らせる |
| `spring.web.resources.static-locations` | `frontend/dist` を配信する |

HTTPS の通信では、CSRF の Cookie にも Secure が付き、HSTS ヘッダー（以後 HTTPS でのみ接続させる指示）も返される。
`http://` で開いた場合は、Cloudflare が `https://` へ転送する。

## 注意

- URL を知っている人は誰でもユーザー登録できる。URL は信頼できる相手にだけ渡す。
- 友人が登録したメールアドレスと記録は、このPCの MySQL に保存される。
- ログインの試行回数の制限はまだ無い。テストが終わったらトンネルを止める。
