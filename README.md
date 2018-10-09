# Play Framework2.6のAPI作成ハンズオン

## API開発概要

以下のAPIを開発します。

* ユーザリスト取得API(GET)
```
curl -XGET http://localhost:9000/api/users/list
```
* ユーザ情報取得API(GET)
```
curl -XGET http://localhost:9000/api/users/show/1
```
* ユーザ新規作成API(POST)
```
curl -H "Content-type: application/json" -XPOST -d '{"name":"Jack Hanma", "companyId":1}' http://localhost:9000/api/users/create
```
* ユーザ情報更新API(POST)
```
curl -H "Content-type: application/json" -XPOST -d '{"id":1, "name":"Katsumi Orochi", "companyId":2}' http://localhost:9000/api/users/update
```
* ユーザ削除API(POST)
```
curl -XPOST http://localhost:9000/api/users/remove/3
```

### 環境情報

* JDK: 1.8.144
* Scala: 2.12.x
* Play Framework: 2.6.x
* Play Slick: 3.0.1
* ScalikeJDBC: 3.2.2
* MySQL: 5.7.x以上

※1. Play SlickはSlickのバージョン3.2.x、Scalaバージョン2.11.x/2.12.x、Playバージョン2.6.xに対応しています。

※2. ScalikeJDBCでもデータベースアクセス可能な例を提示するために利用しています。


## 開発の事前準備

### プロジェクトの作成

`sbt new`でテンプレートからプロジェクトを作成します。
ここでは、`mini-api-play2`という名前でプロジェクトを作ります。

```
sbt new playframework/play-scala-seed.g8 --branch 2.6.x

[info] Loading settings from idea.sbt ...
[info] Loading global plugins from /Users/nagakuray/.sbt/1.0/plugins
[info] Updating {file:/Users/nagakuray/.sbt/1.0/plugins/}global-plugins...
[info] Done updating.
[info] Set current project to samples (in build file:/Users/nagakuray/Desktop/samples/)

This template generates a Play Scala project

name [play-scala-seed]: mini-api-play2
organization [com.example]:

Template applied in ./mini-api-play2
```

### build.sbtの設定

```scala
name := """mini-api-play2"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// libraryDependenciesにライブリを追加します。
libraryDependencies ++= Seq(
  "net.codingwell" %% "scala-guice" % "4.2.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.typesafe.slick" %% "slick-codegen" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "org.scalikejdbc" %% "scalikejdbc" % "3.2.2",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.2.2"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
```

### データベースの設定

#### データベースの作成

```sql
CREATE DATABASE dev_db;
```

#### application.confにデータベース設定を追加

`conf/application.conf`に以下の設定を追加してください。  
`FIXME`となっている箇所を変更してください。

```conf/application.conf
# Slick向けの設定
slick.dbs.default.driver="slick.driver.MySQLDriver$"
slick.dbs.default.db.driver="com.mysql.jdbc.Driver"
slick.dbs.default.db.url="jdbc:mysql://127.0.0.1:3306/dev_db"
slick.dbs.default.db.user=FIXME // 各自の環境に合わせてユーザ名を設定してください。
slick.dbs.default.db.password=FIXME // 各自の環境に合わせてパスワードを設定してください。
play.evolutions.enabled=true

// ScalikeJDBC向けの設定
db.default.driver="com.mysql.cj.jdbc.Driver"
db.default.url="jdbc:mysql://localhost:3306/dev_db?useSSL=false"
db.default.user=FIXME // 各自の環境に合わせてユーザ名を設定してください。
db.default.password=FIXME // 各自の環境に合わせてパスワードを設定してください。
db.default.poolInitialSize=10
db.default.poolMaxSize=20
db.default.poolConnectionTimeoutMillis=1000

// UserRepositoryで利用するExecutionContextの設定
fixedConnectionPool = 5
user.repository.dispatcher {
  executor = "thread-pool-executor"
  throughput = 1
  thread-pool-executor {
    fixed-pool-size = ${fixedConnectionPool}
  }
}
```

#### マイグレーションファイルの作成

ライブラリ`play-slick-evolutions`によって、SQLからテーブルの作成まで実施します。
実行のためのSQLファイルを作成します。

```conf/evolutions/default/1.sql
# --- !Ups
CREATE TABLE IF NOT EXISTS dev_db.companies (
   `id` INTEGER NOT NULL
   ,`name` VARCHAR(20) NOT NULL
   ,PRIMARY KEY(`id`)
);

INSERT INTO dev_db.companies VALUES (1, 'NTTDATA');
INSERT INTO dev_db.companies VALUES (2, 'NTTDOCOMO');

CREATE TABLE IF NOT EXISTS dev_db.users (
   `id` INTEGER(20) AUTO_INCREMENT
  ,`name` VARCHAR(20) NOT NULL
  ,`company_id` INTEGER NOT NULL
  ,PRIMARY KEY (`id`)
  ,FOREIGN KEY (`company_id`) REFERENCES dev_db.companies (`id`)
);

INSERT INTO dev_db.users(`name`, `company_id`) VALUES ('Taro Yamada', 1);
INSERT INTO dev_db.users(`name`, `company_id`) VALUES ('Hanako Sato', 2);

# --- !Downs
DROP TABLE IF EXISTS dev_db.users;
DROP TABLE IF EXISTS dev_db.companies;
```

#### マイグレーションの実行

DBマイグレーションのために、まずplayアプリケーションを起動します。

```
sbt run
```

ブラウザで以下URLに接続して、「Apply this script now!」を押下します。
* http://localhost:9000

これによって、テーブルの作成、初期データの投入ができます。

#### モデルファイルの作成

テーブルに基づいたコードの自動生成は`slick-codegen`を使います。
以下のようなscalaファイルを作成します。
ここでは、`app/Build.scala`を作成しました。

**`TODO`のユーザ名、パスワードは書き換えて利用してください。**
書き換え後、単独のScalaアプリとして実行してください。

```scala
import slick.codegen.SourceCodeGenerator

/**
  * slick-codegenを利用して
  * テーブルからモデルファイルを生成するコードジェネレータファイル
  * http://slick.lightbend.com/doc/3.2.3/code-generation.html
  */
object Build {

  def main(args: Array[String]): Unit = {

    val profile = "slick.jdbc.MySQLProfile"
    val jdbcDriver = "com.mysql.cj.jdbc.Driver"
    val url = "jdbc:mysql://localhost:3306/dev_db?nullNamePatternMatchesAll=true"
    val outputDir = "./app"
    val pkg = "models"

    // TODO: この2行は各自の環境に合わせて書き換えてください。
    val user = Some("username")
    val password = Some("password")

    // ジェネレータの実行
    SourceCodeGenerator.run(profile, jdbcDriver, url, outputDir, pkg, user, password, false)

  }

}
```

実行後すると、`app/models/Tables.scala`が作成されていることが確認できます。

```scala
package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables
...
```

ここまでで開発準備がとりあえず完了しました。

## 開発

### 方針

`app`ディレクトリ配下を以下の構成で作成していきます。
`views`配下の`index.scala.html`, `main.scala.html`および、`controllers`配下の`HomeController`は削除してください。

```
$ tree app

app
├── controllers
│   └── UserController.scala
├── services
│   └── UserService.scala
├── models
│   ├── Tables.scala
│   └── User.scala
├── repositories
│   └── UserRepository.scala
└── views
```

また、上記の構成に付随して、今後編集するファイルは以下になります。
以下の構成は、今回のハンズオン用の構成になります。

|レイヤー| パッケージ名 | ファイル名 | 説明 |
|:---|:----|:-----|:----|
|インフラ層| app/models | Tables.scala | // TODO |
|モデル層| app/models | User.scala | DTO的なクラスを定義します。 |
|リポジトリ層| app/repositories | UserRepository.scala | // TODO|
|サービス層| app/services | UserService.scala | 業務に関するロジックを定義します。|
| - | app/controllers| UserController.scala | ルーティング従ってアクションを定義します。| 
| - | - | conf/routes | ルーティングを定義します。 |
| - | - | app/Module.scala | 依存性の注入(DI)を定義します。 | 

### コントローラの雛形作成

`app/controllers/UserController.scala`の雛形を作成しておきます。
実装は次の節から実施します。

```scala
package controllers

import controllers.UserController.UserForm
import services.UserService
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User


/**
  * ユーザコントローラクラス
  *
  * @param cc ControllerComponentes
  * @param service UserService DIによって外から注入する。
  * @param ec ExecutionContext
  */
class UserController @Inject()(cc: ControllerComponents, service: UserService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {


  /**
    * ユーザ一覧取得
    */
  def list = TODO

  /**
    * ユーザ情報取得
    */
  def show(id: Long) = TODO

  /**
    * ユーザ新規登録
    */
  def create = TODO

  /**
    * ユーザ情報更新
    */
  def update = TODO

  /**
    * ユーザ削除
    */
  def remove(id: Long) = TODO

}

/**
  * UserControllerのコンパニオンオブジェクト
  */
object UserController {
  // TODO
}
```

### ルーティングの定義

`conf/routes`を次のように定義します。
``

```routes
# User API
GET     /api/users/list             controllers.UserController.list
GET     /api/users/show/:id         controllers.UserController.show(id: Long)
POST    /api/users/create           controllers.UserController.create
POST    /api/users/update           controllers.UserController.update
POST    /api/users/remove/:id       controllers.UserController.remove(id: Long)
```

### モデルの作成

`app/models/User.scala`にユーザ情報保持用のケースクラスを作成してください。  
今回は、特にこのケースクラスに振る舞い（メソッド）は持ちませんが、例えばフィールドに`birthday`などを持っている場合に年齢を求める振る舞い(メソッド`getAge`)はここで持たせるのがよいと思います。

```scala
package models

/**
  * User情報を保持するケースクラス
  *
  * @param id          ユーザID
  * @param name        ユーザ名
  * @param companyId   会社ID
  * @param companyName 会社名
  */
final case class User(id: Long, name: String, companyId: Int, companyName: Option[String] = None)
```

### リポジトリの作成

データベースに接続してサービス層とデータをやりとりするためのリポジトリを実装します。
開発対象は`app/repositories/UserRepository.scala`になります。

まずはじめに、`UserRepository`トレイトを用意しておきます。  
これは抽象的なメソッドのみを定義したJavaのインタフェースに相当するものです。

このトレイトの実装クラスは別途用意しなければなりません。

```scala
package repositories

import models.Tables._
import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.MarkerContext
import play.api.libs.concurrent.CustomExecutionContext
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import scalikejdbc._
import scalikejdbc.config._
import scala.concurrent.Future


/**
  * UserRepositoryのトレイト
  * 実装クラスはこのトレイトを継承する。
  */
trait UserRepository {
  def list()(implicit mc: MarkerContext): Future[Seq[User]]

  def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]]

  def insert(user: User)(implicit mc: MarkerContext): Future[Long]

  def update(user: User)(implicit mc: MarkerContext): Future[Long]

  def delete(id: Long)(implicit mc: MarkerContext): Future[Long]
}
```

次に、`UserRepository`トレイトを実装したダミーのデータを返す`UserRepositoryImplWithDummy`クラスを実装してみます。`UserRepository.scala`と同じファイルに定義してみてください。

```scala
/**
  * ダミーデータを返却するためのUserRepositoryの実装クラス
  *
  * @param ec UserRepository用のExecutionContext
  */
class UserRepositoryImplWithDummy @Inject()()(implicit ec: UserRepositoryExecutionContext) extends UserRepository {

  override def list()(implicit mc: MarkerContext): Future[Seq[User]] = Future {
    Seq(
      User(1000, "Baki Hanma", 1),
      User(1001, "Yujiro Hanma", 1),
      User(1002, "Doppo Orochi", 2),
      User(1003, "Izo Motobe", 2),
    )
  }

  override def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]] = Future {
    Some(User(id, "Pickle", 1, Some("NTTDATA")))
  }

  override def insert(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    1000
  }

  override def update(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    1001
  }

  override def delete(id: Long)(implicit mc: MarkerContext): Future[Long] = Future {
    1003
  }
}
```

### ユーザサービスの実装

`app/services/UserService.scala`を実装します。
このサービス層は、コントローラとリポジトリ層の間で業務に必要な処理を記述します。
**今回は単純にUserRepositoryとUserControllerでデータを素通しさせています**。

```scala
package services

import javax.inject.Inject
import models.User
import repositories.UserRepository
import scala.concurrent.Future


trait UserService {
  def getUsers(): Future[Seq[User]]

  def getUser(id: Long): Future[Option[User]]

  def registerUser(user: User): Future[Long]

  def updateUser(user: User): Future[Long]

  def removeUser(id: Long): Future[Long]
}

/**
  * UserServiceの実装クラス
  *
  * @param repository UserRepository DIで注入する
  */
class UserServiceImpl @Inject()(repository: UserRepository) extends UserService {
  override def getUsers(): Future[Seq[User]] = {
    // do something
    repository.list()
  }

  override def getUser(id: Long): Future[Option[User]] = {
    // do something
    repository.find(id)
  }

  override def registerUser(user: User): Future[Long] = {
    // do something
    repository.insert(user)
  }

  override def updateUser(user: User): Future[Long] = {
    // do something
    repository.update(user)
  }

  override def removeUser(id: Long): Future[Long] = {
    // do something
    repository.delete(id)
  }
}
```

ポイントは、実装クラスの以下の部分になります。

```scala
class UserServiceImpl @Inject()(repository: UserRepository) extends UserService {
```

ここの`@Inject()(repository: UserRepository)`で`UserRepository`の実装クラス(`UserRepositoryImplWithDummy`)ではなく、抽象的トレイトを指定しています。**実装に依存せず、抽象に依存する**ようにしています。  
**アプリを動作させるためには、実装クラスのオブジェクトを外側から注入しないといけません**。

この依存解決は、Play Framework（正確にはGoogle Guiceで提供している動的DIコンテナ)の仕組みを使って解決します。
コントローラの実装完了後にDIの定義を設定することにします。

また、本来の業務アプリでいえば、`UserServiceImpl`に対して複数の振る舞いを持たせたい場合、たとえばパスワードの機能を持たせたい場合には、以下のように`PasswordService`トレイトとその実装クラスを作成してUserServiceImplに継承してあげるのがベターなようです。

`app/services/PasswordService.scala`

```scala
package services

import org.mindrot.jbcrypt.BCrypt

// 抽象トレイト
trait PasswordService {
  // ハッシュパスワードを生成する
  def hashPassword(rawPassword: String): String

  // パスワードをチェックする
  def checkPassword(rawPassword: String, hashedPassword: String): Boolean
}

// 実装トレイト
// クラスではなく、トレイトにしているのは多重継承(ミックスイン)するため。
// Scalaではクラスの多重継承はできない。
trait PasswordServiceImpl extends PasswordService {
  override def hashPassword(rawPassword: String): String =
    BCrypt.hashpw(rawPassword, BCrypt.gensalt())

  override def checkPassword(rawPassword: String, hashedPassword: String): Boolean =
    BCrypt.checkpw(rawPassword, hashedPassword)
}
```

`app/services/UserService.scala`

```scala
class UserServiceImpl @Inject()(repository: UserRepository) extends UserService with PasswordServiceImpl {
...
```


### コントローラの実装

コントローラ`UserController`の実装は以下の通りです。
コピペしてください。

```scala
package controllers

import controllers.UserController.UserForm
import services.UserService
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User


/**
  * ユーザコントローラクラス
  *
  * @param cc      ControllerComponents
  * @param service UserService DIによって外から注入する。
  * @param ec      ExecutionContext
  */
class UserController @Inject()(cc: ControllerComponents, service: UserService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {


  /**
    * ユーザ一覧取得
    */
  def list: Action[AnyContent] = Action.async { implicit request =>
    import UserController.usersWritesFormat
    service.getUsers().map { users =>
      Ok(Json.obj("users" -> Json.toJson(users)))
    }
  }

  /**
    * ユーザ情報取得
    */
  def show(id: Long): Action[AnyContent] = Action.async { implicit request =>
    import UserController.userDetailWriteFormat
    service.getUser(id).map {
      case Some(u) => Ok(Json.obj("result" -> Json.toJson(u)))
      case None => Ok(Json.obj("result" -> "User not found"))
    }
  }

  /**
    * ユーザ新規登録
    */
  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      // バリデーションチェックがOKの場合
      val user = User(0, form.name, form.companyId)
      service.registerUser(user).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // バリデーションチェックがNGの場合
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ情報更新
    */
  def update: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      // バリデーションチェックがOKの場合
      val user = User(form.id.get, form.name, form.companyId)
      service.updateUser(user).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // バリデーションチェックがNGの場合
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ削除
    */
  def remove(id: Long): Action[AnyContent] = Action.async { implicit request =>
    service.removeUser(id).map { _ =>
      Ok(Json.obj("result" -> "success"))
    }
  }

}

/**
  * UserControllerのコンパニオンオブジェクト
  */
object UserController {

  case class UserForm(id: Option[Long], name: String, companyId: Int)

  implicit val userFormFormat: Reads[UserForm] = (
    (__ \ "id").readNullable[Long] and
      (__ \ "name").read[String] and
      (__ \ "companyId").read[Int]
    ) (UserForm)

  implicit val usersWritesFormat: Writes[User] = (user: User) => {
    Json.obj(
      "id" -> user.id,
      "name" -> user.name,
      "companyId" -> user.companyId
    )
  }

  implicit val userDetailWriteFormat: Writes[User] = (user: User) => {
    Json.obj(
      "id" -> user.id,
      "name" -> user.name,
      "companyId" -> user.companyId,
      "companyName" -> user.companyName
    )
  }

}
```

ポイントとしては、2点です。

1. `UserService`トレイトが依存性解決の対象になっています。抽象に依存するようになっていることに注目してください。実装である`UserServiceImpl`の注入は別途外側から注入します。

```scala
class UserController @Inject()(cc: ControllerComponents, service: UserService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {
```

2. コンパニオンオブジェクト`UserController`で定義されている暗黙のパラメータ`implicit val userFormFormat`、`implicit val usersWritesFormat`、`implicit val userDetailWriteFormat`は、クライアントからのJSONボディのパラメータを`UserForm`にマッピングしたり、`User`オブジェクトをJSONに書き出すためのパラメータになります。

### 依存性注入の解決

`UserServiceImpl`クラス、`UserController`クラスを定義しましたが、依存性の解決が残っています。以下の部分で放置していた実装クラスのオブジェクト注入です。

```scala
// repositoryに実装クラスUserRepositoryImplWithDummyのオブジェクトを注入するようにしたい
class UserServiceImpl @Inject()(repository: UserRepository) extends UserService {
```

```scala
// serviceに実装クラスUserServiceImplのオブジェクトを注入するようにしたい
class UserController @Inject()(cc: ControllerComponents, service: UserService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {
```

解決するために、`app/Module.scala`を以下のように作成してください。

```scala
import com.google.inject.AbstractModule
import services._
import javax.inject._
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import repositories._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure(): Unit = {
    // UserRepositoryのDI
    bind[UserRepository].to[UserRepositoryImplWithDummy].in[Singleton]

    // UserServiceのDI
    bind[UserService].to[UserServiceImpl].in[Singleton]
  }
}
```

ソースコードから`UserRepository`に`UserRepositoryImplWithDummy`を注入していること、および`UserService`に`UserServiceImpl`を注入していることが何となくわかると思います。  
また、`Singleton`を指定することによって、アプリ起動時に一度だけ生成されるようになっています（これは、オブジェクトの生成コストが高いためです）。


### アプリの起動

実際にアプリを起動して結果が取得できるかAPIを叩いて確認します。  
まずアプリを起動してください。

```
sbt run
```

起動が完了したら、`curl`コマンドでAPIを叩きます。

* ユーザリスト取得API(GET)
```
curl -XGET http://localhost:9000/api/users/list
```
* ユーザ情報取得API(GET)
```
curl -XGET http://localhost:9000/api/users/show/1
```
* ユーザ新規作成API(POST)
```
curl -H "Content-type: application/json" -XPOST -d '{"name":"Jack Hanma", "companyId":1}' http://localhost:9000/api/users/create
```
* ユーザ情報更新API(POST)
```
curl -H "Content-type: application/json" -XPOST -d '{"id":1, "name":"Katsumi Orochi", "companyId":2}' http://localhost:9000/api/users/update
```
* ユーザ削除API(POST)
```
curl -XPOST http://localhost:9000/api/users/remove/1
```

### データベースアクセスの実装追加 - ScalikeJDBCで接続

// TODO


### データベースアクセスの実装追加 - Slickで接続

// TODO