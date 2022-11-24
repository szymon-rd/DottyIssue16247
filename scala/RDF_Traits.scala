package rdfscala

trait TstNode:
   def value(): String

trait Lit extends TstNode
trait BNode extends TstNode
trait URI extends TstNode

trait ScalaMkNodes:
   def mkBNode(): BNode
   def mkUri(uriStr: String): URI
   def mkLit(lit: String): Lit
end ScalaMkNodes   

object SimpleScalaNodeFactory extends ScalaMkNodes:
   var counter = 0
   def mkBNode(): BNode = new BNode{
      val c = {
         val oldc = counter
         counter = counter+1
         oldc
      }   
      def value(): String = "_:"+c
   }
   def mkLit(lit: String): Lit = new Lit{
      def value(): String = lit
   }
   def mkUri(uriStr: String): URI = new URI{
      def value(): String = uriStr
   }
end SimpleScalaNodeFactory   
