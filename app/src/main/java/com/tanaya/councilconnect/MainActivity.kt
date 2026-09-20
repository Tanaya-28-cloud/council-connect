package com.tanaya.councilconnect

import androidx.compose.material3.ExperimentalMaterial3Api
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.tanaya.councilconnect.data.local.AppDatabase
import com.tanaya.councilconnect.data.model.Event
import com.tanaya.councilconnect.data.remote.FirestoreRepository
import com.tanaya.councilconnect.data.repository.EventRepository
import com.tanaya.councilconnect.ui.screen.EventDetailScreen
import com.tanaya.councilconnect.ui.theme.CouncilConnectTheme
import com.tanaya.councilconnect.ui.viewmodel.EventViewModel
import com.tanaya.councilconnect.ui.viewmodel.EventViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EventViewModel

    private var selectedEvent by mutableStateOf<Event?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("CouncilFCM", "FCM token: ${task.result}")
            } else {
                Log.e("CouncilFCM", "Failed to get FCM token", task.exception)
            }
        }

        if (
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        val database = AppDatabase.getInstance(applicationContext)

        val firestoreRepository = FirestoreRepository()

        val eventRepository = EventRepository(
            eventDao = database.eventDao(),
            rsvpDao = database.rsvpDao(),
            firestoreRepository = firestoreRepository
        )

        viewModel = ViewModelProvider(
            this,
            EventViewModelFactory(eventRepository)
        )[EventViewModel::class.java]

        setContent {
            CouncilConnectTheme {

                if (selectedEvent == null) {

                    EventListScreen(
                        viewModel = viewModel,
                        onEventClick = { event ->
                            selectedEvent = event
                        }
                    )

                } else {

                    EventDetailScreen(
                        event = selectedEvent!!,
                        onBack = {
                            selectedEvent = null
                        },
                        onRsvp = {
                            viewModel.submitRsvp(selectedEvent!!)
                        },
                        onCheckIn = {
                            viewModel.submitCheckIn(selectedEvent!!)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    onEventClick: (Event) -> Unit
) {
    val events by viewModel.events.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Council Connect",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Student Council Events",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if (events.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Loading events...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        text = "Upcoming Events",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(events) { event ->

                    EventCard(
                        event = event,
                        onClick = {
                            onEventClick(event)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📍 ${event.location}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "👥 ${event.rsvpCount} RSVPs",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap to view details →",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}