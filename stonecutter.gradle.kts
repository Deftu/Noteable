plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"

    replacements.string(eval(current.version, "<1.21.11")) {
        replace("net.minecraft.Util", "net.minecraft.util.Util")
    }
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
