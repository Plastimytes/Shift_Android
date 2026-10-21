package app.shift.learn.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.shift.learn.AppViewModel
import app.shift.learn.ui.home.HomeScreen
import app.shift.learn.ui.lesson.LessonScreen
import app.shift.learn.ui.onboarding.OnboardingScreen
import app.shift.learn.ui.quiz.QuizScreen
import app.shift.learn.ui.section.SectionScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ABOUT = "about"
    const val BUILD = "build"
    const val REPAIR = "repair"
    const val LESSON = "lesson/{id}"
    const val QUIZ = "quiz/{id}"
    fun lesson(id: String) = "lesson/$id"
    fun quiz(id: String) = "quiz/$id"
}

private data class Tab(val route: String, val label: String, val fullName: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Home", "Home", Icons.Filled.Home),
    Tab(Routes.ABOUT, "About", "About Cars", Icons.Filled.Info),
    Tab(Routes.BUILD, "Build", "Build the Car", Icons.Filled.Build),
    Tab(Routes.REPAIR, "Repair", "Repair and Modify", Icons.Filled.Settings),
)

@Composable
fun ShiftRoot(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val content by vm.content.collectAsStateWithLifecycle()
    val error by vm.loadError.collectAsStateWithLifecycle()
    val completed by vm.completed.collectAsStateWithLifecycle()
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()

    val p = prefs
    val c = content
    if (error != null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text(error.orEmpty()) }
        return
    }
    if (p == null || c == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val showBar = route in TABS.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBar) NavigationBar {
                TABS.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = {
                            nav.navigate(t.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(t.icon, contentDescription = t.fullName) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = if (p.onboardingDone) Routes.HOME else Routes.ONBOARDING,
            modifier = Modifier.padding(inner),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(c, selection, vm::toggleInterest) {
                    vm.finishOnboarding()
                    nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            }
            composable(Routes.HOME) {
                HomeScreen(p, c, completed, bookmarks, vm::setMode, { nav.navigate(Routes.lesson(it)) }, vm::toggleBookmark)
            }
            for ((r, section, title) in listOf(
                Triple(Routes.ABOUT, "about-cars", "About Cars"),
                Triple(Routes.BUILD, "build-the-car", "Build the Car"),
                Triple(Routes.REPAIR, "repair-and-modify", "Repair and Modify"),
            )) {
                composable(r) { SectionScreen(title, section, c, completed) { nav.navigate(Routes.lesson(it)) } }
            }
            composable(Routes.LESSON, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val lesson = c.lessons.firstOrNull { it.id == entry.arguments?.getString("id") }
                if (lesson == null) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Lesson not found") }
                else LessonScreen(
                    lesson = lesson, mode = p.mode, sources = c.sources,
                    bookmarked = lesson.id in bookmarks, completed = lesson.id in completed,
                    onMode = vm::setMode, onBack = { nav.popBackStack() },
                    onBookmark = { vm.toggleBookmark(lesson.id) }, onQuiz = { nav.navigate(Routes.quiz(lesson.id)) },
                )
            }
            composable(Routes.QUIZ, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val lesson = c.lessons.firstOrNull { it.id == entry.arguments?.getString("id") }
                if (lesson == null) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Lesson not found") }
                else QuizScreen(lesson, { nav.popBackStack() }, { vm.recordQuiz(lesson, it) })
            }
        }
    }
}
