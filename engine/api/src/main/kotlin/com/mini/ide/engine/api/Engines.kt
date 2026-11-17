package com.mini.ide.engine.api

import kotlinx.coroutines.flow.SharedFlow

interface EditorEngine
interface RenderEngine
interface BridgeEngine
interface ProjectEngine

data class EditorEvent(val fileId: String, val content: String)
