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
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


data class RuleSet(
    val id: String,
    val text: String,
    val children: List<RuleSet>)



fun loadRules(file :File):RuleSet{
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = builder.parse(file)
    return buildRules(doc.childNodes.item(0))!!
}

fun buildRules(document: Node):RuleSet? {
    val rules = mutableListOf<RuleSet>()
    val id = document.nodeName
    if (!id.startsWith("r")){ return null }
    val text = document.attributes.getNamedItem("text")?.textContent ?: document.attributes.getNamedItem("t").textContent
    for(i in 0 until document.childNodes.length){
        val doc = document.childNodes.item(i)
        buildRules(doc)?.let { rules.add(it) }
    }
    return RuleSet(
        id = id,
        text = text,
        children = rules)
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
    div.div("float-left") {//everything else
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
        div {
            text(rules.id + ":" + rules.text)
            id = "children-${rules.id}"
            for (child in rules.children) {
                div("child-${child.id}") {
                    addRules(this, child)
                }
            }
        }
    }
}

fun main() {
    writeRulesToHtmlFile(loadRules(File("rules.xml")),File("docs/index.html"))
}