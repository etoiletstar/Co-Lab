package com.colab.spatial

import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.pico.spatial.ui.foundation.dsl.WindowContainer
import com.pico.spatial.ui.foundation.dsl.WindowContainerSize
import com.pico.spatial.ui.foundation.dsl.Form
import com.pico.spatial.ui.foundation.dsl.Placement
import androidx.compose.ui.unit.dp
import com.colab.spatial.content.*

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                WorkbenchPage()
            }
        }
        WindowContainer(id = Windows.Canvas, form = Form.Planar,
            defaultSize = WindowContainerSize(1280.dp, 1080.dp),
            placement = { containers.firstOrNull { it.id == Windows.Workbench }?.let { Placement.right(it, 24.dp) } ?: Placement.none() }
        ) { PicoTheme { CanvasPage() } }
        WindowContainer(id = Windows.Details, form = Form.Planar,
            defaultSize = WindowContainerSize(760.dp, 1000.dp),
            placement = { containers.firstOrNull { it.id == Windows.Workbench }?.let { Placement.left(it, 24.dp) } ?: Placement.none() }
        ) { PicoTheme { DetailsPage() } }
        WindowContainer(id = Windows.Assistant, form = Form.Planar,
            defaultSize = WindowContainerSize(800.dp, 960.dp),
            placement = { containers.firstOrNull { it.id == Windows.Workbench }?.let { Placement.bottom(it, 24.dp) } ?: Placement.none() }
        ) { PicoTheme { AssistantPage() } }
        WindowContainer(id = Windows.Artifacts, form = Form.Planar,
            defaultSize = WindowContainerSize(840.dp, 1000.dp),
            placement = { containers.firstOrNull { it.id == Windows.Workbench }?.let { Placement.top(it, 24.dp) } ?: Placement.none() }
        ) { PicoTheme { ArtifactsPage() } }
    }
