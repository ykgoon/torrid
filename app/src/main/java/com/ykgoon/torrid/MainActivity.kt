package com.ykgoon.torrid

import android.net.Uri
import android.os.Bundle
import com.meta.spatial.castinputforward.CastInputForwardFeature
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.SpatialFeature
import com.meta.spatial.core.Vector3
import com.meta.spatial.okhttp3.OkHttpAssetFetcher
import com.meta.spatial.runtime.NetworkedAssetLoader
import com.meta.spatial.runtime.ReferenceSpace
import com.meta.spatial.runtime.SceneMaterial
import com.meta.spatial.toolkit.AppSystemActivity
import com.meta.spatial.toolkit.Material
import com.meta.spatial.toolkit.Mesh
import com.meta.spatial.toolkit.PanelRegistration
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.vr.VRFeature
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppSystemActivity() {
    private var gltfxEntity: Entity? = null
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun registerFeatures(): List<SpatialFeature> {
        val features = mutableListOf<SpatialFeature>(VRFeature(this))
        if (BuildConfig.DEBUG) {
            features.add(CastInputForwardFeature(this))
        }
        return features
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkedAssetLoader.init(
            File(applicationContext.getCacheDir().canonicalPath), OkHttpAssetFetcher())

        // wait for GLXF to load before accessing nodes inside it
        loadGLXF().invokeOnCompletion {
            // get the environment mesh from Cosmo and set it to use an unlit shader.
            val composition = glXFManager.getGLXFInfo("entity_prime")
            val environmentEntity: Entity? = composition.getNodeByName("Environment").entity
            val environmentMesh = environmentEntity?.getComponent<Mesh>()
            environmentMesh?.defaultShaderOverride = SceneMaterial.UNLIT_SHADER
            environmentEntity?.setComponent(environmentMesh!!)
        }
    }

    override fun onSceneReady() {
        super.onSceneReady()

        // set the reference space to enable recentering
        scene.setReferenceSpace(ReferenceSpace.LOCAL_FLOOR)

        scene.setLightingEnvironment(
            ambientColor = Vector3(0f),
            sunColor = Vector3(7.0f, 7.0f, 7.0f),
            sunDirection = -Vector3(1.0f, 3.0f, -2.0f),
            environmentIntensity = 0.3f)
        scene.updateIBLEnvironment("environment.env")

        scene.setViewOrigin(0.0f, 0.0f, 2.0f, 180.0f)

        Entity.create(
            listOf(
                Mesh(Uri.parse("mesh://skybox")),
                Material().apply {
                    baseTextureAndroidResourceId = R.drawable.skydome
                    unlit = true // Prevent scene lighting from affecting the skybox
                },
                Transform(Pose(Vector3(x = 0f, y = 0f, z = 0f)))))
    }

    private fun loadGLXF(): Job {
        gltfxEntity = Entity.create()
        return activityScope.launch {
            glXFManager.inflateGLXF(
                Uri.parse("apk:///scenes/Composition.glxf"),
                rootEntity = gltfxEntity!!,
                keyName = "entity_prime")
        }
    }

    override fun registerPanels(): List<PanelRegistration> {
        return listOf(
            PanelRegistration(R.layout.prime) {
                config {
                    themeResourceId = R.style.PanelAppThemeTransparent
                    includeGlass = false
                    width = 2.0f
                    height = 1.5f
                    enableLayer = true
                    enableTransparent = true
                }
            })
    }
}