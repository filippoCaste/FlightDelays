package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private Graph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	
	public Model() {
		this.dao = new ExtFlightDelaysDAO();
		this.idMap = new HashMap<Integer, Airport>(); // viene creata una sola volta, non per ogni grafo
		
		// riempimento Map (tutti) 
		// 		[potrebbe esserne creata una apposita con solo gli aeroporti necessari]
		this.dao.loadAllAirports(idMap);
	}

	/**
	 * i vertici devono 
		rappresentare gli aeroporti su cui 
		operano almeno x compagnie aeree (in 
		arrivo o in partenza), e gli archi devono 
		rappresentare le rotte tra gli aeroporti collegati tra di loro da almeno un volo. 
		Il peso dell’arco deve 
		rappresentare il numero totale di voli tra i due aeroporti
	 * @param x indica il numero minimo di compragnie aeree che operano tra i due aeroporti
	 */
	public void creaGrafo(int x) { 
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// aggiunta dei vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));

		// aggiunta degli archi
		for(Rotta r : dao.getRotte(this.idMap)) {
			// controllo che gli aeroporti siano effettivamente presenti nel grafo
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge dwe = this.grafo.getEdge(r.getA1(), r.getA2());
				if(dwe == null) {

					// se arco ancora non esistente
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli());
				} else {

					// se arco già presente, aggiorno il peso considerando la rotta inversa
					double pesoOld = this.grafo.getEdgeWeight(dwe);
					double pesoNew = pesoOld + r.getnVoli();

					this.grafo.setEdgeWeight(dwe, pesoNew);
				}
			}
		}
		
		System.out.println("Numero di vertici: " + this.grafo.vertexSet().size() +
				"\nNumero di archi: " + this.grafo.edgeSet().size());
	}
	
	public List<Airport> getVertici() {
		if(grafo!=null) {
			List<Airport> vertici = new ArrayList<>(this.grafo.vertexSet());
			Collections.sort(vertici);
			return vertici;
		} else {
			throw new RuntimeException("Grafo non ancora creato");
		}
	}
	
	// punto d
	public List<Airport> getPercorso(Airport a1, Airport a2) {
		List<Airport> percorso = new ArrayList<>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, a1);
		
		// verificare che i due vertici facciano parte di una stessa componente connessa
		// altrimenti potrebbe esserci un loop infinito
		
		boolean trovato = false;
		
		// visita del grafo
		while(it.hasNext()) {
			Airport visitato = it.next();
			if(visitato.equals(a2)) {
				trovato = true;
			}
		}
		
		// ottengo il percorso da destinazione all'insù 
		if(trovato) {
			percorso.add(a2);
			Airport step = it.getParent(a2);

			while(!step.equals(a1)) {
				percorso.add(0,step); // aggiungo in testa così già è ordinato
				step = it.getParent(step);
			}

			percorso.add(0,a1); // aggiungo la sorgente

			return percorso;
		} else {
			return null; // non fanno parte della stessa componente connessa
		}
	}

}
