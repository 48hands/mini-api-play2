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
    val user = Some("nagakuray")
    val password = Some("bakuman01")

    SourceCodeGenerator.run(profile, jdbcDriver, url, outputDir, pkg, user, password, false)

  }

}
