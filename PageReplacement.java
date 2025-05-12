// State variables
        let currentStep = 0;
        let pageRefs = [];
        let frames = [];
        let stats = {
            hits: 0,
            faults: 0
        };
        let algorithm = 'fifo';
        let frameCount = 3;
        let fifoQueue = [];
        let lruCounters = {};
        let isSimulating = false;
        let simulationInterval = null;
        
        // DOM elements
        const pageRefVisual = document.getElementById('pageRefVisual');
        const framesVisual = document.getElementById('framesVisual');
        const simulateBtn = document.getElementById('simulateBtn');
        const stepBtn = document.getElementById('stepBtn');
        const resetBtn = document.getElementById('resetBtn');
        const algorithmSelect = document.getElementById('algorithm');
        const frameCountInput = document.getElementById('frameCount');
        const pageRefStringInput = document.getElementById('pageRefString');
        const totalRefsEl = document.getElementById('totalRefs');
        const pageHitsEl = document.getElementById('pageHits');
        const pageFaultsEl = document.getElementById('pageFaults');
        const faultRateEl = document.getElementById('faultRate');
        const algorithmExplanationEl = document.getElementById('algorithmExplanation');
        
        // Algorithm explanations
        const algorithmExplanations = {
            fifo: "FIFO (First In First Out) replaces the page that has been in memory the longest when a new page needs to be loaded.",
            lru: "LRU (Least Recently Used) replaces the page that hasn't been used for the longest time when a new page needs to be loaded.",
            optimal: "Optimal algorithm replaces the page that won't be used for the longest time in the future. This is theoretical as it requires knowledge of future references."
        };
        
        // Initialize the simulation
        function initSimulation() {
            // Parse inputs
            algorithm = algorithmSelect.value;
            frameCount = parseInt(frameCountInput.value);
            pageRefs = pageRefStringInput.value.split(',').map(Number);
            
            // Reset state
            currentStep = 0;
            frames = Array(frameCount).fill(null);
            stats = { hits: 0, faults: 0 };
            fifoQueue = [];
            lruCounters = {};
            
            // Update stats display
            updateStats();
            
            // Update algorithm explanation
            algorithmExplanationEl.textContent = algorithmExplanations[algorithm];
            
            // Render visualization
            renderPageRefVisual();
            renderFramesVisual();
        }
        
        // Update statistics display
        function updateStats() {
            const total = pageRefs.length;
            const hitRate = total > 0 ? Math.round((stats.hits / total) * 100) : 0;
            const faultRate = total > 0 ? Math.round((stats.faults / total) * 100) : 0;
            
            totalRefsEl.textContent = total;
            pageHitsEl.textContent = stats.hits;
            pageFaultsEl.textContent = stats.faults;
            faultRateEl.textContent = `${faultRate}%`;
        }
        
        // Render page reference visualization
        function renderPageRefVisual() {
            pageRefVisual.innerHTML = '';
            
            pageRefs.forEach((ref, index) => {
                const refEl = document.createElement('div');
                refEl.className = 'page-ref w-8 h-8 flex items-center justify-center border border-gray-300 rounded-md';
                refEl.textContent = ref;
                
                if (index === currentStep) {
                    refEl.classList.add('active');
                }
                
                pageRefVisual.appendChild(refEl);
            });
        }
        
        // Render frames visualization
        function renderFramesVisual() {
            framesVisual.innerHTML = '';
            
            for (let i = 0; i < frameCount; i++) {
                const frameRow = document.createElement('div');
                frameRow.className = 'flex items-center gap-4';
                
                const frameLabel = document.createElement('div');
                frameLabel.className = 'w-8 text-right font-medium';
                frameLabel.textContent = `Frame ${i+1}:`;
                frameRow.appendChild(frameLabel);
                
                const frameEl = document.createElement('div');
                frameEl.className = 'frame w-12 h-12 flex items-center justify-center border border-gray-300 rounded-md text-xl font-bold';
                frameEl.textContent = frames[i] !== null ? frames[i] : '-';
                frameRow.appendChild(frameEl);
                
                framesVisual.appendChild(frameRow);
            }
        }
        
        // Perform one step of the simulation
        function simulationStep() {
            if (currentStep >= pageRefs.length) {
                stopSimulation();
                return;
            }
            
            const page = pageRefs[currentStep];
            let isHit = false;
            
            // Check if page is already in a frame (hit)
            if (frames.includes(page)) {
                isHit = true;
                stats.hits++;
                
                // For LRU, update the counter
                if (algorithm === 'lru') {
                    lruCounters[page] = currentStep;
                }
            } 
            // Page fault - need to replace a page
            else {
                stats.faults++;
                
                // Find an empty frame first
                const emptyFrameIndex = frames.indexOf(null);
                
                if (emptyFrameIndex !== -1) {
                    frames[emptyFrameIndex] = page;
                    
                    // For FIFO, add to queue
                    if (algorithm === 'fifo') {
                        fifoQueue.push(emptyFrameIndex);
                    }
                    
                    // For LRU, set counter
                    if (algorithm === 'lru') {
                        lruCounters[page] = currentStep;
                    }
                } 
                // No empty frames, need to replace
                else {
                    let replaceIndex;
                    
                    if (algorithm === 'fifo') {
                        // FIFO replacement
                        replaceIndex = fifoQueue.shift();
                        frames[replaceIndex] = page;
                        fifoQueue.push(replaceIndex);
                    } 
                    else if (algorithm === 'lru') {
                        // LRU replacement - find page with smallest counter
                        let minCounter = Infinity;
                        let lruPage = null;
                        
                        for (const [p, counter] of Object.entries(lruCounters)) {
                            if (counter < minCounter) {
                                minCounter = counter;
                                lruPage = parseInt(p);
                            }
                        }
                        
                        replaceIndex = frames.indexOf(lruPage);
                        frames[replaceIndex] = page;
                        lruCounters[page] = currentStep;
                    } 
                    else if (algorithm === 'optimal') {
                        // Optimal replacement - find page not used for longest time in future
                        let farthest = -1;
                        let optimalPage = null;
                        
                        for (const p of frames) {
                            let nextUse = -1;
                            
                            // Find next use of this page in remaining references
                            for (let i = currentStep + 1; i < pageRefs.length; i++) {
                                if (pageRefs[i] === p) {
                                    nextUse = i;
                                    break;
                                }
                            }
                            
                            // If page won't be used again, it's the best candidate
                            if (nextUse === -1) {
                                optimalPage = p;
                                break;
                            }
                            
                            // Otherwise track the farthest next use
                            if (nextUse > farthest) {
                                farthest = nextUse;
                                optimalPage = p;
                            }
                        }
                        
                        replaceIndex = frames.indexOf(optimalPage);
                        frames[replaceIndex] = page;
                    }
                }
            }
            
            // Update visualization
            updateVisualization(isHit);
            
            // Move to next step
            currentStep++;
            
            // Update stats
            updateStats();
            
            // If we've reached the end, stop simulation
            if (currentStep >= pageRefs.length) {
                stopSimulation();
            }
        }
        
        // Update visualization after a step
        function updateVisualization(isHit) {
            // Update page reference highlights
            const pageRefElements = pageRefVisual.querySelectorAll('.page-ref');
            pageRefElements.forEach((el, index) => {
                el.classList.remove('active', 'hit', 'fault');
                
                if (index === currentStep) {
                    el.classList.add(isHit ? 'hit' : 'fault');
                } else if (index === currentStep + 1) {
                    el.classList.add('active');
                }
            });
            
            // Update frames visualization
            const frameElements = framesVisual.querySelectorAll('.frame');
            frameElements.forEach((el, index) => {
                el.classList.remove('active', 'hit', 'fault');
                el.textContent = frames[index] !== null ? frames[index] : '-';
                
                // Highlight the frame that was involved in the hit/fault
                if (!isHit && frames[index] === pageRefs[currentStep]) {
                    el.classList.add('fault');
                } else if (isHit && frames[index] === pageRefs[currentStep]) {
                    el.classList.add('hit');
                }
            });
        }
        
        // Start simulation
        function startSimulation() {
            if (isSimulating) return;
            
            isSimulating = true;
            simulateBtn.innerHTML = '<i class="fas fa-pause mr-2"></i> Pause';
            simulateBtn.classList.remove('bg-blue-600');
            simulateBtn.classList.add('bg-yellow-500');
            
            simulationInterval = setInterval(() => {
                simulationStep();
            }, 1000);
        }
        
        // Stop simulation
        function stopSimulation() {
            if (!isSimulating) return;
            
            isSimulating = false;
            clearInterval(simulationInterval);
            simulateBtn.innerHTML = '<i class="fas fa-play mr-2"></i> Simulate';
            simulateBtn.classList.remove('bg-yellow-500');
            simulateBtn.classList.add('bg-blue-600');
        }
        
        // Event listeners
        simulateBtn.addEventListener('click', () => {
            if (currentStep === 0) {
                initSimulation();
            }
            
            if (isSimulating) {
                stopSimulation();
            } else {
                startSimulation();
            }
        });
        
        stepBtn.addEventListener('click', () => {
            if (currentStep === 0) {
                initSimulation();
            }
            
            if (!isSimulating) {
                simulationStep();
            }
        });
        
        resetBtn.addEventListener('click', () => {
            stopSimulation();
            initSimulation();
        });
        
        algorithmSelect.addEventListener('change', () => {
            algorithmExplanationEl.textContent = algorithmExplanations[algorithmSelect.value];
        });
        
        // Initialize
        initSimulation();
