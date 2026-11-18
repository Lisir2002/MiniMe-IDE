package com.mini.ide.extension.api

/**
 * 扩展点标记接口（S7）。
 *
 * M0–M5 仅契约占位；M6 落地最小可用的 [ExtensionRegistry] 并把语言提供方建模为扩展点，
 * 以验证扩展机制可运行。ShareHandler / CollabProvider 仍只留契约、不实现、不注册。
 */
interface ExtensionPoint

/** 分享处理器扩展点（契约保留，暂不实现）。 */
interface ShareHandler : ExtensionPoint

/** 语言提供方扩展点：把一组语言档案贡献给宿主。 */
interface LanguageProvider : ExtensionPoint {
    fun languageIds(): List<String>
}

/**
 * 扩展注册表：S7 的最小激活实现。
 *
 * - 纯 JVM，无 Android / Hilt 依赖，可穷举单测；
 * - 按类型过滤（`all<T>()`），调用方只认得扩展点接口，不依赖具体实现类；
 * - 同名类型后注册覆盖先注册，保证确定性。
 */
class ExtensionRegistry {
    private val points = mutableMapOf<Class<out ExtensionPoint>, ExtensionPoint>()

    fun <T : ExtensionPoint> register(point: T) {
        points[point.javaClass] = point
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ExtensionPoint> all(type: Class<T>): List<T> =
        points.values.filter { type.isInstance(it) } as List<T>

    inline fun <reified T : ExtensionPoint> all(): List<T> = all(T::class.java)

    fun clear() = points.clear()

    val size: Int get() = points.size
}
