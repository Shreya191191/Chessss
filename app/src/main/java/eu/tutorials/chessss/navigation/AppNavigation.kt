package eu.tutorials.chessss.ui.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import eu.tutorials.chessss.ui.screens.GameScreen
import eu.tutorials.chessss.ui.screens.HomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(navController)
        }

        composable("game") {
            println("Button Clicked")
            GameScreen()
        }
    }
}
