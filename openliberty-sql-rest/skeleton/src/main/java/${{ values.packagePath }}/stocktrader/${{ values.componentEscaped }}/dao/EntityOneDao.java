/*
       Copyright 2025 Kyndryl Corp, All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.kyndryl.cjot.stocktrader.myapp.dao;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.RequestScoped;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.stream.Stream;

import com.kyndryl.cjot.stocktrader.myapp.entities.EntityOne;

// TODO move to Jakarta Data
@RequestScoped
public class EntityOneDao {

    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    private TypedQuery<EntityOne> query;

    @WithSpan
    public void createEntityOne(EntityOne entityOne) {
        em.persist(entityOne);
    }

    @WithSpan
    public EntityOne readEvent(String owner) {
        return em.find(EntityOne.class, owner);
    }

    @WithSpan
    public void updateEntityOne(EntityOne entityOne) {
        em.merge(entityOne);
        em.flush();
    }

    @WithSpan
    public void deleteEntityOne(EntityOne entityOne) {
        em.remove(em.merge(entityOne));
    }

    public Stream<EntityOne> readAllEntityOnes() {
        if(query==null){
            query = em.createNamedQuery("EntityOne.findAll", EntityOne.class);
        }
        return query.getResultStream();
    }

    @WithSpan
    public Stream<EntityOne> getPageOfEntityOnes(int pageNumber, int pageSize) {
        // From https://www.baeldung.com/jpa-pagination
        if (query==null){
            query = em.createNamedQuery("EntityOne.findAll", EntityOne.class);
        }
        query.setFirstResult((pageNumber - 1) * pageSize);
        return query.setMaxResults(pageSize).getResultStream();
    }

}