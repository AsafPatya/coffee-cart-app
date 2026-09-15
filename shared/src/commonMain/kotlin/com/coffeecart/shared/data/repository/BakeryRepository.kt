package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.BakeryDto
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.toModel
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.BakeryRepositoryInterface
import com.coffeecart.shared.model.Bakery
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class BakeryRepository(
    private val client: HttpClient,
) : BakeryRepositoryInterface {
    override suspend fun getBakeries(): List<Bakery> =
        client.get("${ServerEnvironment.baseUrl}${Endpoints.BAKERIES}").body<List<BakeryDto>>().map { it.toModel() }
}
