package org.othercraft.rules

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.stream.appendHTML
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


data class RuleSet(
    val id: String,
    val text: String,
    val desc: String?,
    val children: List<RuleSet>)



fun loadRules(file :File):RuleSet{
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = builder.parse(file)!!
    return buildRules(doc.childNodes.item(0))!!
}

fun buildRules(document: Node):RuleSet? {
    val rules = mutableListOf<RuleSet>()
    val id = document.nodeName
    if (!id.startsWith("r") && id != "head"){
        return null
    }
    val text = if(id != "head")
        document.attributes.getNamedItem("text")?.textContent ?: document.attributes.getNamedItem("t").textContent
            else
        "null"
    val desc = document.attributes.getNamedItem("desc")?.textContent
    for(i in 0 until document.childNodes.length){
        val doc = document.childNodes.item(i)
        buildRules(doc)?.let { rules.add(it) }
    }
    return RuleSet(
        id = id.substring(1),
        text = text,
        children = rules,
        desc = desc)
}


fun writeRulesToHtmlFile(rules: RuleSet,file: File){
    fun intoStream(doc: Element, out: OutputStream) {
        with(TransformerFactory.newInstance().newTransformer()){
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            setOutputProperty(OutputKeys.METHOD, "xml")
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transform(
                DOMSource(doc),
                StreamResult(OutputStreamWriter(out, "UTF-8"))
            )
        }
    }
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    val html = document.create.html {
        head {
            meta {
                content = "text/html;charset=utf-8"
                httpEquiv = "Content-Type"
            }
            title("Planet Sim II Rules")
            this.styleLink("css.css")
        }
        body {
            h1 {+"Planet Sim II Rules" }
            div("rules") {
                addRules(this,rules)
            }
        }
    }
    intoStream(html, file.outputStream())
}


fun addRules(div: DIV,rules: RuleSet){
    fun DIV.addChildren(){
        for (child in rules.children) {
            div("child-${child.id}") {
                addRules(this, child)
            }
        }
    }
    if (rules.id == "ead"){
        div.addChildren()
        return
    }
    div.div("float-left") {//everything else
        div {
            button(classes = "link-like") {
                text("[-]")
                onClick = """
                let x = document.getElementById("children-${rules.id}");
                if(x.style.display === "none"){
                    x.style.display = "block"
                }else {
                    x.style.display = "none";
                }
            """.trimIndent()
            }
            button(classes = "link-like"){
                text("link")
                onClick = """
                    let a = window.location.href.split('#')[0];
                    let b = "" + a + "#children-${rules.id}"
                    navigator.clipboard.writeText(b);
                """.trimIndent()
            }
            strong {
                text(rules.id)
            }
            text(rules.text)
        }
        if(rules.children.isNotEmpty() || rules.desc != null)
            div {
                rules.desc?.let { i("float-left") { text(it) } }
                id = "children-${rules.id}"
                addChildren()
            }
    }
}

fun <T> List<T>.repeat(times: Int):List<T>{
    val mut = mutableListOf<T>()
    for (i in 0 until times){
        mut.addAll(this)
    }
    return mut
}

fun main() {
    writeRulesToHtmlFile(loadRules(File("generated-rules.xml")),File("docs/index.html"))
}

fun mainf(){
    var str = "<head>\n"

    for (a in 1 until 10){
        str += "<r$a text=\"overview\">\n"
        for (b in 1 until 10){
            str += "<r$a.$b text=\"this is a rule\">\n"
            for(c in 1 until 10){
                str += "<r$a.$b.$c text=\"this is another rule\" />\n"
            }
            str += "</r$a.$b>\n"
        }
        str += "</r$a>\n"
    }
    str += "</head>"
    File("generated-rules.xml").writeText(str)
}