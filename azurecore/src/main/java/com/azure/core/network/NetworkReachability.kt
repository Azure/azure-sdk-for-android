package com.azure.core.network

import java.net.InetAddress

class NetworkReachability {
    val address : InetAddress

    constructor(name : String) : this(InetAddress.getByName(name))
    constructor(address : InetAddress){
        this.address = address
        address.isReachable()
    }


}