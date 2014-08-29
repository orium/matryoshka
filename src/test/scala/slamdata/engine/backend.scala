package slamdata.engine

import org.specs2.mutable._

import slamdata.engine.fp._

class BackendSpecs extends Specification with DisjunctionMatchers {
  "interpretPaths" should {
    import slamdata.engine.fs.{Path}
    import slamdata.engine.sql._

    "make simple table name relative to base path" in {
      val q = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(TableRelationAST("bar", None)),
        None, None, None, None, None)
      val mountPath = Path("/")
      val basePath = Path("/foo/")
      val exp = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(TableRelationAST("./foo/bar", None)),
        None, None, None, None, None)

      (new SQLParser).interpretPaths(q, mountPath, basePath) must beRightDisj(exp)
    }

    "make sub-query table names relative to base path" in {
      val q = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(SubqueryRelationAST(
          SelectStmt(
            Proj(Wildcard, None) :: Nil,
            Some(TableRelationAST("bar", None)),
            None, None, None, None, None), "t")),
        None, None, None, None, None)
      val mountPath = Path("/")
      val basePath = Path("/foo/")
      val exp = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(SubqueryRelationAST(
          SelectStmt(
            Proj(Wildcard, None) :: Nil,
            Some(TableRelationAST("./foo/bar", None)),
            None, None, None, None, None), "t")),
        None, None, None, None, None)

      (new SQLParser).interpretPaths(q, mountPath, basePath) must beRightDisj(exp)
    }

    "make join table names relative to base path" in {
      val q = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(JoinRelation(
          TableRelationAST("bar", None),
          TableRelationAST("baz", None),
          LeftJoin,
          Ident("id")
        )),
        None, None, None, None, None)
      val mountPath = Path("/")
      val basePath = Path("/foo/")
      val exp = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(JoinRelation(
          TableRelationAST("./foo/bar", None),
          TableRelationAST("./foo/baz", None),
          LeftJoin,
          Ident("id")
        )),
        None, None, None, None, None)

      (new SQLParser).interpretPaths(q, mountPath, basePath) must beRightDisj(exp)
    }

    "make cross table names relative to base path" in {
      val q = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(CrossRelation(
          TableRelationAST("bar", None),
          TableRelationAST("baz", None))),
        None, None, None, None, None)
      val mountPath = Path("/")
      val basePath = Path("/foo/")
      val exp = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(CrossRelation(
          TableRelationAST("./foo/bar", None),
          TableRelationAST("./foo/baz", None))),
        None, None, None, None, None)

      (new SQLParser).interpretPaths(q, mountPath, basePath) must beRightDisj(exp)
    }

    "make sub-select table names relative to base path" in {
      val q = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(TableRelationAST("bar", None)),
        Some(Binop(
          Ident("widgetId"),
          Subselect(
            SelectStmt(
              Proj(Ident("id"), None) :: Nil,
              Some(TableRelationAST("widget", None)),
              None, None, None, None, None)),
          In)),
        None, None, None, None)
      val mountPath = Path("/")
      val basePath = Path("/foo/")
      val exp = SelectStmt(
        Proj(Wildcard, None) :: Nil,
        Some(TableRelationAST("./foo/bar", None)),
        Some(Binop(
          Ident("widgetId"),
          Subselect(
            SelectStmt(
              Proj(Ident("id"), None) :: Nil,
              Some(TableRelationAST("./foo/widget", None)),
              None, None, None, None, None)),
          In)),
        None, None, None, None)

      (new SQLParser).interpretPaths(q, mountPath, basePath) must beRightDisj(exp)
    }
  }

}
