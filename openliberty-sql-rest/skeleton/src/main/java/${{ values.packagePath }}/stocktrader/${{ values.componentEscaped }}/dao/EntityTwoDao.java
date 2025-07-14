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

package ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.dao;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.RequestScoped;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import ${{ values.package }}.stocktrader.${{ values.componentEscaped }}.entities.EntityTwo;

@RequestScoped
public class EntityTwoDao {

    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    @WithSpan
    public void createEntityTwo(EntityTwo entityTwo) {
        em.persist(entityTwo);
    }

    @WithSpan
    public EntityTwo readEvent(String symbol) {
        return em.find(EntityTwo.class, symbol);
    }

    @WithSpan
    public void EntityTwo(EntityTwo entityTwo) {
        em.merge(entityTwo);
        em.flush();
    }

    @WithSpan
    public void deleteEntityTwo(EntityTwo entityTwo) {
        em.remove(em.merge((entityTwo)));
    }

    @WithSpan
    public void detachEntityTwo(EntityTwo entityTwo) {
        em.detach(entityTwo);
    }

    @WithSpan
    public void updateEntityTwo(EntityTwo entityTwo) {
        em.merge(entityTwo);
        em.flush();
    }

    @WithSpan
    public List<EntityTwo> readEntityTwoDaoByOwner(String owner) {
        return em.createNamedQuery("EntityTwo.findByOwner", EntityTwo.class)
            .setParameter("owner", owner).getResultList();
    }

    @WithSpan
    public List<EntityTwo> readEntityTwoDaoByOwnerAndSymbol(String owner, String symbol) {
        return em.createNamedQuery("EntityTwo.findByOwnerAndSymbol", EntityTwo.class)
            .setParameter("owner", owner)
            .setParameter("symbol", symbol).getResultList();
    }
}