package org.nimio.app.feature.account.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AccountApiConverterTest {

    @Test
    fun `retrofit builds request body converter for register request`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(
                Json {
                    ignoreUnknownKeys = true
                }.asConverterFactory("application/json".toMediaType())
            )
            .build()

        val converter: Converter<RegisterRequestDto, RequestBody> = retrofit.requestBodyConverter(
            RegisterRequestDto::class.java,
            emptyArray<Annotation>(),
            emptyArray<Annotation>()
        )

        val body = converter.convert(
            RegisterRequestDto(
                email = "test@example.com",
                password = "SecurePass123!",
                username = "tester",
                displayName = "Test User"
            )
        )

        assertNotNull(body)
    }
}



