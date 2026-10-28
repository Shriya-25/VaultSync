import React, { useEffect, useRef } from 'react';

export default function AmbientGlow() {
  const glowWrapperRef = useRef(null);
  const animFrameRef = useRef(null);
  const idleTimeoutRef = useRef(null);

  // Store coordinates & opacity without triggering React re-renders
  const stateRef = useRef({
    currentX: typeof window !== 'undefined' ? window.innerWidth / 2 : 0,
    currentY: typeof window !== 'undefined' ? 260 : 0,
    targetX: typeof window !== 'undefined' ? window.innerWidth / 2 : 0,
    targetY: typeof window !== 'undefined' ? 260 : 0,
    currentOpacity: 0.18, // Mostly black when idle with faint ambient tint
    targetOpacity: 0.18,
  });

  useEffect(() => {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;

    if (prefersReducedMotion || isTouchDevice) {
      return;
    }

    const state = stateRef.current;
    const glowEl = glowWrapperRef.current;
    if (!glowEl) return;

    state.currentX = window.innerWidth / 2;
    state.currentY = 280;
    state.targetX = state.currentX;
    state.targetY = state.currentY;

    const handlePointerMove = (e) => {
      state.targetX = e.clientX;
      state.targetY = e.clientY;
      state.targetOpacity = 0.70; // Noticeable medium-intensity olive glow while moving

      if (idleTimeoutRef.current) {
        clearTimeout(idleTimeoutRef.current);
      }

      // Gentle fade down when the cursor stops, keeping background mostly black
      idleTimeoutRef.current = setTimeout(() => {
        state.targetOpacity = 0.18;
      }, 500);
    };

    const handlePointerLeave = () => {
      state.targetOpacity = 0.08;
    };

    window.addEventListener('pointermove', handlePointerMove, { passive: true });
    document.addEventListener('mouseleave', handlePointerLeave);

    const animate = () => {
      // Fluid cursor following with a slight delay
      const lerpFactor = 0.065;
      state.currentX += (state.targetX - state.currentX) * lerpFactor;
      state.currentY += (state.targetY - state.currentY) * lerpFactor;

      // Gentle, smooth opacity transitions
      state.currentOpacity += (state.targetOpacity - state.currentOpacity) * 0.04;

      if (glowEl) {
        glowEl.style.transform = `translate3d(${state.currentX.toFixed(1)}px, ${state.currentY.toFixed(1)}px, 0) translate(-50%, -50%)`;
        glowEl.style.opacity = state.currentOpacity.toFixed(3);
      }

      animFrameRef.current = requestAnimationFrame(animate);
    };

    animFrameRef.current = requestAnimationFrame(animate);

    return () => {
      window.removeEventListener('pointermove', handlePointerMove);
      document.removeEventListener('mouseleave', handlePointerLeave);
      if (animFrameRef.current) {
        cancelAnimationFrame(animFrameRef.current);
      }
      if (idleTimeoutRef.current) {
        clearTimeout(idleTimeoutRef.current);
      }
    };
  }, []);

  return (
    <div className="vs-ambient-glow-wrapper" aria-hidden="true">
      {/* Static atmospheric background glow */}
      <div className="vs-static-ambient-glow"></div>

      {/* Fluid cursor-following spotlight (Dual-layer organic diffusion) */}
      <div ref={glowWrapperRef} className="vs-cursor-glow-spotlight">
        {/* Layer 1: Wide feathered outer aura */}
        <div className="vs-glow-aura"></div>
        {/* Layer 2: Vibrant, bright luminous core */}
        <div className="vs-glow-core"></div>
      </div>
    </div>
  );
}
