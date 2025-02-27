package de.timo_reymann.mjml_support.reference

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttribute

class MjmlPathAttributeTypeReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement().inside(XmlAttributeValue::class.java).inFile(MJML_FILE_PATTERN)
            .inFile(MJML_FILE_PATTERN),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val attribute = element.parentOfType<XmlAttribute>() ?: return arrayOf()

                    val (_, mjmlAttribute) = getMjmlInfoFromAttribute(attribute)
                    if (mjmlAttribute?.type != MjmlAttributeType.PATH) {
                        return arrayOf()
                    }

                    val filename = attribute.value ?: return arrayOf()
                    val virtualFile =
                        VfsUtilCore.findRelativeFile(filename, element.containingFile.virtualFile) ?: return arrayOf()

                    if (virtualFile.isDirectory) {
                        return arrayOf()
                    }

                    return arrayOf(
                        FileReference(
                            FileReferenceSet.createSet(element, false, false, false),
                            TextRange(0, attribute.value!!.length + 1), 0, attribute.value
                        )
                    )
                }
            })
    }
}
