/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.app.logic.startup

import org.dbflute.util.Srl
import java.io.File
import java.io.IOException
import java.util.*

/**
 * @author jflute
 */
class StartupLogic {

    fun fromHarbor(projectDir: File, domain: String, serviceName: String) {
        val packageName = buildPackageName(domain)
        NewProjectCreator("harbor", projectDir, object : NewProjectCreator.ServiceNameFilter {
            override fun filter(original: String): String {
                var filtered = original
                filtered = replace(filtered, buildProjectDirPureName(projectDir), Srl.initUncap(serviceName)) // e.g. lastaflute-example-harbor
                filtered = replace(filtered, "lastaflute-example-harbor", Srl.initUncap(serviceName)) // just in case
                filtered = replace(filtered, "maihamadb", Srl.initUncap(serviceName) + if (!serviceName.endsWith("db")) "db" else "")
                filtered = replace(filtered, "org/docksidestage", replace(packageName, ".", "/")) // for file path
                filtered = replace(filtered, "docksidestage.org", domain)
                filtered = replace(filtered, "org.docksidestage", packageName)
                filtered = replace(filtered, "Harbor", Srl.initCap(serviceName))
                filtered = replace(filtered, "harbor", Srl.initUncap(serviceName))
                filtered = replace(filtered, "new JettyBoot(8090, ", "new JettyBoot(9001, ")
                filtered = replace(filtered, "new TomcatBoot(8090, ", "new TomcatBoot(9001, ")
                return filtered
            }
        }).newProject()
    }

    protected fun buildPackageName(domain: String): String { // e.g. docksidestage.org to org.docksidestage
        val elementList = ArrayList(Arrays.asList(*domain.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        elementList.reverse()
        val pkgName = elementList.stream().reduce { left, right -> "$left.$right" }.get()
        return pkgName.replace("-", "") // e.g. org.dockside-stage to org.docksidestage
    }

    private fun buildProjectDirPureName(projectDir: File): String { // e.g. /sea/mystic => mystic
        try {
            return Srl.substringLastRear(projectDir.canonicalPath, "/") // thanks oreilly
        } catch (e: IOException) {
            throw IllegalStateException("Failed to get canonical path: $projectDir")
        }

    }

    protected fun replace(str: String, fromStr: String, toStr: String): String {
        return Srl.replace(str, fromStr, toStr)
    }
}