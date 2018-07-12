/*******************************************************************************
* Copyright (c) 2018 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.faultTolerance;

import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import com.acmeair.client.FlightClient;

@ApplicationScoped
public class FlightClientConnection {
  
  private static  Logger logger =  Logger.getLogger(FlightClientConnection.class.getName());
  
  @Inject
  private FlightClient flightClient;
  
  // TODO: Do we really need all of these?
  //@Bulkhead(value = 50, waitingTaskQueue = 300)
  @Retry(maxRetries=6,delayUnit=ChronoUnit.SECONDS,delay=10,durationUnit=ChronoUnit.MINUTES,maxDuration=5)
  @Fallback(LongFallbackHandler.class)
  @CircuitBreaker(delay=10,delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 3, failureRatio = 1.0)
  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  public Long connect(String jwtToken, String userId, String flightSegId, boolean add) throws ConnectException, TimeoutException,CircuitBreakerOpenException,InterruptedException{
    int executionCounter = 0;
    
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("FlightClientFTConnectionBean.connect()  called: ");
    }
    
    try {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Delay Duration: " + 
            "10 seconds" + " FlightClientFTConnectionBean.connect() Service called, execution " + executionCounter);
      }
      executionCounter++;
      
      Long miles = flightClient.getRewardMiles(jwtToken, flightSegId).getMiles();
            
      if (!add ) {
        miles = miles * -1;
      }
      
      return miles;
      
    } catch (Exception e) {
      e.printStackTrace();
      executionCounter = 0;
      return null;
    }
  }
}