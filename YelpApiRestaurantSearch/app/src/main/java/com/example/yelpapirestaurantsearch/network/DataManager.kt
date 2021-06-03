package com.example.yelpapirestaurantsearch.network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery
import com.example.yelpapirestaurantsearch.models.ErrorResponse
import com.example.yelpapirestaurantsearch.models.RestaurantSearchQueryRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


/***
 * The [DataManager] singleton class is used to create a central instance of the Appollo client
 * and route all data/network requests to the Yelp GraphQL API.
 */
object DataManager {

    private const val YELP_API = "https://api.yelp.com/v3/graphql"
    val logTAG = "DataManager"

    fun getApolloClient(authToken: String): ApolloClient {
        return ApolloClient.builder()
            .serverUrl(YELP_API)
            .okHttpClient(getOkHttpClient(authToken))
            .build()
    }

    private fun getOkHttpClient(authToken: String): OkHttpClient{

        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()

        return okHttpClientBuilder
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(NetworkRequestInterceptor(authToken = authToken))
            .build()
    }

    class NetworkRequestInterceptor(val authToken: String): Interceptor{

        override fun intercept(chain: Interceptor.Chain): Response {

            val requestBuilder: Request.Builder = chain.request()
                .newBuilder()
                .header("Authorization","Bearer $authToken")

            val request: Request = requestBuilder.build()

            return chain.proceed(request);
        }
    }

    suspend fun getRestaurantsList(request: RestaurantSearchQueryRequest,
                                   authToken: String): Any?{
        Log.v(logTAG, "Request restaurant data")
        val response = try {
            getApolloClient(authToken).query(SearchRestaurantsQuery(
                latitude = Input.fromNullable(request.latitude),
                longitude = Input.fromNullable(request.longitude),
                radius = Input.fromNullable(request.radius),
                category = Input.fromNullable(request.category)
            )).await()
        }catch (e : ApolloException){
            Log.d(logTAG, "Apollo Request Failure", e)
            return ErrorResponse("Error", "Failed to obtain the restaurant" +
                    " List. Seems to be a Apollo Request Failure")
        }

        if (response.errors == null) {
            return response.data?.search
        }

        return ErrorResponse("Error - No Data", "Failed to obtain the restaurant" +
                " List. No Data")
    }
}