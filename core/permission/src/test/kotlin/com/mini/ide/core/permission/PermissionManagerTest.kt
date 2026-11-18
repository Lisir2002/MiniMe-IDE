package com.mini.ide.core.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeChecker(private val granted: Set<AppPermission> = emptySet()) : PermissionChecker {
    override fun isGranted(permission: AppPermission) = permission in granted
}

class AppPermissionTest {

    @Test
    fun `六大功能域齐全`() {
        assertEquals(6, AppPermission.DOMAINS.size)
    }

    @Test
    fun `每个权限都有非空用途说明`() {
        AppPermission.entries.forEach { p ->
            assertTrue("${p.name} 用途不能为空", p.purpose.isNotBlank())
            assertTrue("${p.name} manifest 名不能为空", p.manifestName.isNotBlank())
        }
    }

    @Test
    fun `权限分类正确`() {
        assertEquals(PermissionKind.NORMAL, AppPermission.NETWORK.kind)
        assertEquals(PermissionKind.DANGEROUS, AppPermission.CAMERA.kind)
        assertEquals(PermissionKind.SPECIAL, AppPermission.STORAGE_MANAGE.kind)
        assertEquals(PermissionKind.SPECIAL, AppPermission.INSTALL_UNKNOWN_APP.kind)
    }

    @Test
    fun `预留权限不主动申请`() {
        assertFalse(AppPermission.LOCATION.proactive)
        assertFalse(AppPermission.CAMERA.proactive)
        assertFalse(AppPermission.INSTALL_UNKNOWN_APP.proactive)
        assertTrue(AppPermission.STORAGE_MANAGE.proactive)
    }

    @Test
    fun `API适用范围正确`() {
        assertTrue(AppPermission.STORAGE_MANAGE.appliesTo(33))
        assertFalse(AppPermission.STORAGE_MANAGE.appliesTo(28))
        assertTrue(AppPermission.STORAGE_LEGACY.appliesTo(28))
        assertFalse(AppPermission.STORAGE_LEGACY.appliesTo(33))
    }

    @Test
    fun `special权限带设置页action`() {
        assertTrue(AppPermission.STORAGE_MANAGE.settingsAction!!.contains("MANAGE_APP_ALL_FILES"))
        assertTrue(AppPermission.INSTALL_UNKNOWN_APP.settingsAction!!.contains("UNKNOWN_APP_SOURCES"))
    }
}

class PermissionManagerTest {

    @Test
    fun `normal权限恒为已授且无需流程`() {
        val mgr = PermissionManager(FakeChecker())
        assertEquals(GrantResult.GRANTED, mgr.status(AppPermission.NETWORK))
        assertEquals(PermissionFlow.NONE, mgr.flow(AppPermission.NETWORK))
        assertFalse(mgr.needsRationale(AppPermission.NETWORK))
    }

    @Test
    fun `dangerous未授首次需rationale且走运行时弹窗`() {
        val mgr = PermissionManager(FakeChecker())
        val p = AppPermission.STORAGE_LEGACY
        assertEquals(GrantResult.DENIED, mgr.status(p))
        assertTrue(mgr.needsRationale(p))
        assertEquals(PermissionFlow.RUNTIME_DIALOG, mgr.flow(p))
        mgr.markRequested(p)
        assertFalse(mgr.needsRationale(p)) // 再次不再弹 rationale
    }

    @Test
    fun `已授dangerous无需流程`() {
        val p = AppPermission.CAMERA
        val mgr = PermissionManager(FakeChecker(setOf(p)))
        assertEquals(GrantResult.GRANTED, mgr.status(p))
        assertEquals(PermissionFlow.NONE, mgr.flow(p))
    }

    @Test
    fun `special未授走设置页`() {
        val mgr = PermissionManager(FakeChecker())
        assertEquals(PermissionFlow.SETTINGS, mgr.flow(AppPermission.STORAGE_MANAGE))
        assertEquals(PermissionFlow.SETTINGS, mgr.flow(AppPermission.INSTALL_UNKNOWN_APP))
    }

    @Test
    fun `reset后重新需要rationale`() {
        val p = AppPermission.STORAGE_LEGACY
        val mgr = PermissionManager(FakeChecker())
        mgr.markRequested(p)
        assertFalse(mgr.needsRationale(p))
        mgr.reset()
        assertTrue(mgr.needsRationale(p))
    }
}
