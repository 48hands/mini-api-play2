# Play Framework2.6のAPI作成ハンズオン

## API開発概要

以下のAPIを開発します。

* ユーザリスト取得API(GET)
* ユーザ情報取得API(GET)
* ユーザ新規作成API(POST)
* ユーザ情報更新API(PUT)
* ユーザ削除API(POST)

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
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.typesafe.slick" %% "slick-codegen" % "3.2.0",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "org.scalikejdbc" %% "scalikejdbc" % "3.2.2"
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

```conf/application.conf
slick.dbs.default.driver="slick.driver.MySQLDriver$"
slick.dbs.default.db.driver="com.mysql.jdbc.Driver"
slick.dbs.default.db.url="jdbc:mysql://127.0.0.1:3306/dev_db"
slick.dbs.default.db.user=FIXME // 各自のパスワードに変更してください。
slick.dbs.default.db.password=FIXME // 各自のパスワードに変更してください。

play.evolutions.enabled=true
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
   `id` INTEGER AUTO_INCREMENT
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
書き換え後、実行してください。

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
`views`配下の`index.scala.html`, `main.scala.html`および、`controllers`配下の`HomeController`は削除してください。

```
$ tree app

app
├── Build.scala
├── controllers
│   └── UserController.scala
├── domain
│   └── UserService.scala
├── models
│   └── Tables.scala
├── repositories
│   └── UserRepository.scala
└── views
```

また、上記の構成に付随して、今後編集するファイルは以下になります。

|レイヤー| パッケージ名 | ファイル名 | 説明 |
|:---|:----|:-----|:----|
|インフラ層| app/models | Tables.scala | // TODO |
|リポジトリ層| app/repositories | UserRepository.scala | // TODO|
|ドメイン層| app/domain | UserService.scala | 業務に関するロジックを定義します。|
| - | app/controllers| UserController.scala | ルーティング従ってアクションを定義します。| 
| - | - | conf/routes | ルーティングを定義します。 |
| - | - | app/Module.scala | 依存性の注入(DI)を定義します。 | 

### コントローラの雛形作成

`app/controllers/UserController.scala`の雛形を作成しておきます。
実装は次の節から実施します。

```scala
package controllers

import domain.UserService
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import repositories.User

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
  def list: Action[AnyContent] = TODO

  /**
    * ユーザ情報取得
    */
  def show(id: Long): Action[AnyContent] = TODO

  /**
    * ユーザ新規登録
    */
  def create: Action[AnyContent] = TODO

  /**
    * ユーザ情報更新
    */
  def update: Action[AnyContent] = TODO

  /**
    * ユーザ削除
    */
  def remove(id: Long): Action[AnyContent] = TODO

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

### リポジトリの作成

データベースに接続してサービス層とデータをやりとりするためのリポジトリを実装します。
開発対象は`app/repositories/UserRepository.scala`になります。

まずはじめに、ユーザ情報を格納するための、`User`ケースクラス、`UserRepository`トレイトを用意します。

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

// ユーザ情報を保持するケースクラス
final case class User(id: Long, name: String,
                      companyId: Option[Int],
                      companyName: Option[String] = None)

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

ポイントは、以下になります。

* TODO
* TODO
* TODO

次に、UserRepositoryトレイトを実装したダミーのデータを返す`UserRepositoryImplWithDummy`クラスを実装してみます。`UserRepository.scala`と同じファイルに定義してみてください。

```scala
**
  * ダミーデータを返却するためのUserRepositoryの実装クラス
  *
  * @param ec UserRepository用のExecutionContext
  */
class UserRepositoryImplWithDummy @Inject()()(implicit ec: UserRepositoryExecutionContext) extends UserRepository {

  override def list()(implicit mc: MarkerContext): Future[Seq[User]] = Future {
    Seq(
      User(1000, "Baki Hanma", Some(1)),
      User(1001, "Yujiro Hanma", Some(1)),
      User(1002, "Doppo Orochi", Some(2)),
      User(1003, "Izo Motobe", None),
    )
  }

  override def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]] = Future {
    Some(User(id, "Pickle", Some(1), Some("NTTDATA")))
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

### ユーザ一覧APIの実装

usersテーブルから全件取得し,IDの昇順でJSONを返却するAPIを実装します。

```scala
// TODO
```

