package com.bestid.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Punto de entrada de iOS: lo monta ContentView.swift. */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
