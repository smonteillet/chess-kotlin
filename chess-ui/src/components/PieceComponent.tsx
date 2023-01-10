import sprite from '../assets/Chess_Pieces_Sprite.svg'
import styled from 'styled-components';
import React from 'react';
import {Square} from '../domain/Square';
import {Rank, ranks} from '../domain/Rank';
import {File, files} from '../domain/File';
import {Position} from '../domain/Position';
import Size from '../ui/Size';


type PieceDrawingCoordinate = {
    top : number,
    left : number,
}

export type PieceComponentProps = {
    position : Position,
}

export type SvgContainerProps = {
   coordinate : PieceDrawingCoordinate
}

const SvgContainer = styled.svg<SvgContainerProps>`
  position: absolute;
  top: ${(svgContainerProps : SvgContainerProps) => `${svgContainerProps.coordinate.top}px`};
  left: ${(svgContainerProps : SvgContainerProps) => `${svgContainerProps.coordinate.left}px`};
`

const PieceComponent : React.FC<PieceComponentProps> = (props) => {
    return <SvgContainer coordinate={squareToPieceDrawingCoordinate(props.position.square)}>
        <use href={`${sprite}#${props.position.color}-${props.position.piece}`} />
    </SvgContainer>
}

const squareToPieceDrawingCoordinate = (square: Square): PieceDrawingCoordinate => {
    const rankIndex = ranks.slice().reverse().indexOf(parseInt(square[1]) as Rank)
    const fileIndex = files.indexOf(square[0] as File)
    return {
        top: (rankIndex) * Size.squareSize + Size.svgPieceSquareOffset,
        left: (fileIndex) * Size.squareSize + Size.svgPieceSquareOffset,
    }
}
export default PieceComponent
