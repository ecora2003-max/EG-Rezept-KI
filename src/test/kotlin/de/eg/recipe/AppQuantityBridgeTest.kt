package de.eg.recipe
object AppQuantityBridgeTest {
    private fun line(n:Long?,u:RecipeEngine.UnitCode=RecipeEngine.UnitCode.G,
        b:RecipeEngine.Basis=RecipeEngine.Basis.NET,id:String?="SYNTHETIC-ID",
        confirmed:Boolean=true,piece:Long?=null,state:RecipeEngine.State=RecipeEngine.State.RAW)=
        AppQuantityBridge.SourceLine("synthetic-food",id,confirmed,RecipeEngine.Quantity(n,u,b,false,state),piece)
    @JvmStatic fun main(args:Array<String>){
        var passed=0
        fun test(fn:()->Unit){fn();passed++;println("PASS app-unit case "+passed)}
        test{val p=AppQuantityBridge.project(line(200));check(p.amountMilli==200000L&&p.structurallyMappable&&p.sourceBasis==RecipeEngine.Basis.NET&&p.sourceState==RecipeEngine.State.RAW&&p.sourceFoodTypeId=="synthetic-food")}
        test{val p=AppQuantityBridge.project(line(125,RecipeEngine.UnitCode.ML));check(p.amountMilli==125000L&&p.unit==AppQuantityBridge.HostUnit.MILLILITRE)}
        test{val p=AppQuantityBridge.project(line(2,RecipeEngine.UnitCode.PORTION,RecipeEngine.Basis.PER_PORTION));check(p.amountMilli==2000L)}
        test{val p=AppQuantityBridge.project(line(null));check(p.amountMilli==null&&AppQuantityBridge.Gap.UNKNOWN_AMOUNT in p.issues)}
        test{val p=AppQuantityBridge.project(line(2,RecipeEngine.UnitCode.PIECE,RecipeEngine.Basis.PIECE));check(p.amountMilli==null&&AppQuantityBridge.Gap.UNSUPPORTED_PIECE_SIZE in p.issues)}
        test{val p=AppQuantityBridge.project(line(2,RecipeEngine.UnitCode.PIECE,RecipeEngine.Basis.PIECE,piece=75000));check(p.amountMilli==150000L)}
        test{val p=AppQuantityBridge.project(line(1000000001));check(p.amountMilli==null&&AppQuantityBridge.Gap.AMOUNT_OVERFLOW in p.issues)}
        test{val p=AppQuantityBridge.project(line(Long.MAX_VALUE));check(p.amountMilli==null&&AppQuantityBridge.Gap.AMOUNT_OVERFLOW in p.issues)}
        test{val p=AppQuantityBridge.project(line(100,confirmed=false));check(!p.structurallyMappable&&AppQuantityBridge.Gap.UNCONFIRMED_IDENTITY in p.issues)}
        test{val p=AppQuantityBridge.project(line(100,id=null));check(!p.structurallyMappable&&AppQuantityBridge.Gap.UNRESOLVED_PRODUCT_VERSION in p.issues)}
        test{val p=AppQuantityBridge.project(line(100,state=RecipeEngine.State.UNKNOWN));check(!p.structurallyMappable&&AppQuantityBridge.Gap.UNKNOWN_PREPARATION_STATE in p.issues)}
        test{val p=AppQuantityBridge.project(line(100,b=RecipeEngine.Basis.GROSS));check(p.amountMilli==null&&AppQuantityBridge.Gap.INCOMPATIBLE_UNIT_BASIS in p.issues)}
        test{for(n in listOf(0L,-1L)){val p=AppQuantityBridge.project(line(n));check(p.amountMilli==null&&AppQuantityBridge.Gap.INVALID_QUANTITY in p.issues)}}
        test{val p=AppQuantityBridge.project(line(2,RecipeEngine.UnitCode.ML,RecipeEngine.Basis.PIECE));check(p.amountMilli==null&&AppQuantityBridge.Gap.INCOMPATIBLE_UNIT_BASIS in p.issues)}
        println("SUMMARY app_quantity_bridge passed=$passed failed=0")
    }
}
