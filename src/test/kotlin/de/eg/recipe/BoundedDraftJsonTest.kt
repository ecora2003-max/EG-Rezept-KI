package de.eg.recipe
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate

object BoundedDraftJsonTest {
    private val request = RecipeEngine.Request("JSON-TEST","2026-10-31T18:00:00+01:00",
        LocalDate.parse("2026-11-03"),2,mealType="MAIN",equipment=setOf("HOB","POT"))
    private val message = RecipeEngine.propose(request,RecipeEngine.RecipeFacts(emptyMap())).messages.first()
    private val json = JsonOutput.encode(message)
    private fun reject(text:String,limits:BoundedDraftJson.Limits=BoundedDraftJson.Limits()){
        var rejected=false
        try { BoundedDraftJson.decode(text,limits) } catch (_:IllegalArgumentException){rejected=true}
        check(rejected)
    }
    @JvmStatic fun main(args:Array<String>){
        var passed=0
        fun test(name:String,fn:()->Unit){fn();passed++;println("PASS "+name)}
        test("generated draft survives full JSON roundtrip"){
            val d=BoundedDraftJson.importDraft(json)
            check(d.portions==2 && d.ingredients.isNotEmpty() && d.requiresIndependentValidation)
        }
        test("decoded draft revalidated to untrusted review"){
            val v=RecipeDraftRevalidation.revalidate(BoundedDraftJson.decode(json),request,
                RecipeEngine.RecipeFacts(emptyMap()))
            check(v.messages.isNotEmpty())
            val x=(v.messages.first()["payload"] as Map<*,*>)["xRecipeKi"] as Map<*,*>
            check(x["verification"]=="REVIEW_REQUIRED")
        }
        test("exact JSON integer representations"){
            val o=BoundedDraftJson.decode("""{"a":2,"b":9223372036854775807,"c":9223372036854775808}""")
            check(o["a"] is Int && o["b"] is Long && o["c"]==BigInteger("9223372036854775808"))
        }
        test("noninteger decimal remains BigDecimal"){
            val o=BoundedDraftJson.decode("""{"a":2.0,"b":-1.25e+2}""")
            check(o["a"] is BigDecimal && o["b"]==BigDecimal("-1.25e+2"))
        }
        test("null booleans and Unicode literal"){
            val o=BoundedDraftJson.decode("""{"a":null,"b":true,"c":false,"d":"Öl 🥑"}""")
            check(o["a"]==null && o["b"]==true && o["c"]==false && o["d"]=="Öl 🥑")
        }
        test("escaped surrogate pair decodes"){
            check(BoundedDraftJson.decode("""{"s":"\uD83E\uDD51"}""")["s"]=="🥑")
        }
        test("valid whitespace and escapes roundtrip"){
            val expected="x\n\"y\"\\"
            check(BoundedDraftJson.decode(" \n "+JsonOutput.encode(mapOf("s" to expected))+" \t ")["s"]==expected)
        }
        test("all eight flags roundtrip"){
            for(bits in 0..7){
                val flags=RecipeEngine.Flags(bits and 1!=0,bits and 2!=0,bits and 4!=0)
                val m=RecipeEngine.propose(request.copy(flags=flags),
                    RecipeEngine.RecipeFacts(emptyMap())).messages.first()
                check(BoundedDraftJson.importDraft(JsonOutput.encode(m)).flags==flags)
            }
        }
        test("duplicate top level key rejected"){reject("""{"x":1,"x":2}""")}
        test("duplicate nested key rejected"){reject("""{"x":{"a":1,"a":2}}""")}
        test("trailing JSON and trailing garbage rejected"){reject("""{"a":1}[]""");reject("""{"a":1}bad""")}
        test("leading zero and illegal plus rejected"){reject("""{"a":01}""");reject("""{"a":+2}""")}
        test("incomplete numbers and exponents rejected"){
            for(v in listOf("-","2.","2e","2e+",".3","3..2"))reject("{\"a\":"+v+"}")
        }
        test("NaN infinity and comments rejected"){
            for(v in listOf("NaN","Infinity","/*x*/1","1//x"))reject("{\"a\":"+v+"}")
        }
        test("unescaped controls and illegal escapes rejected"){
            reject("{\"a\":\"bad\nstring\"}");reject("""{"a":"\q"}""")
        }
        test("unpaired escaped surrogates rejected"){
            reject("""{"a":"\uD83E"}""");reject("""{"a":"\uDD51"}""")
            reject("""{"a":"\uD83E\u0031"}""")
        }
        test("unpaired literal surrogate rejected"){
            reject("{\"a\":\""+0xD83E.toChar()+"\"}");reject("{\"a\":\""+0xDD51.toChar()+"\"}")
        }
        test("trailing commas rejected"){reject("""{"a":1,}""");reject("""{"a":[1,]}""")}
        test("truncated JSON primitive root and double commas rejected"){
            reject("""{"a":[1,2}""");reject("[]");reject("""{"a":[1,,2]}""")
        }
        test("depth ceiling enforced"){
            reject("""{"a":[[[[0]]]]}""",BoundedDraftJson.Limits(maxDepth=3))
        }
        test("node ceiling enforced"){
            reject("""{"a":[1,2,3,4,5,6]}""",BoundedDraftJson.Limits(maxNodes=5))
        }
        test("character ceiling enforced"){reject(json,BoundedDraftJson.Limits(maxChars=100))}
        test("string size ceiling enforced"){
            reject("""{"a":"abcdefg"}""",BoundedDraftJson.Limits(maxStringChars=5))
        }
        test("numeric precision and exponent ceilings enforced"){
            reject("""{"a":1e10000000}""");reject("{\"a\":"+"9".repeat(129)+"}")
        }
        println("SUMMARY bounded_draft_json passed=$passed failed=0")
    }
}
